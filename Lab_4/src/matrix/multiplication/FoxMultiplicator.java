package matrix.multiplication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FoxMultiplicator implements IMatrixMultiplicator {
    private static final int THRESHOLD = 500;

    @Override
    public int[][] multiplyClassic(int[][] a, int[][] b, int threadCount) {
        if (isNotSquare(a) || isNotSquare(b) || a.length != b.length) {
            throw new IllegalArgumentException("Матриці мають бути квадратними і однаковими за розмірами");
        }

        var n = a.length;
        var q = (int) Math.sqrt(threadCount);

        if (q * q != threadCount) {
            throw new IllegalArgumentException("Кількість потоків має бути квадратом");
        }

        if (n % q != 0) {
            throw new IllegalArgumentException("Розмір матриці має ділитись націло на корінь з кількості потоків");
        }

        var blockSize = n / q;
        var A = splitMatrix(a, blockSize, q);
        var B = splitMatrix(b, blockSize, q);
        var C = new int[q][q][blockSize][blockSize];

        var executor = Executors.newFixedThreadPool(threadCount);

        for (int step = 0; step < q; step++) {
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < q; i++) {
                var shift = (i + step) % q;

                for (int j = 0; j < q; j++) {
                    final var fi = i;
                    final var fJ = j;
                    final var fShift = shift;

                    var future = executor.submit(() -> {
                        int[][] product;
                        try {
                            product = multiplyBasic(A[fi][fShift], B[fShift][fJ], 1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        synchronized (C[fi][fJ]) {
                            C[fi][fJ] = add(C[fi][fJ], product);
                        }
                    });

                    futures.add(future);
                }
            }

            for (var future : futures) {
                try {
                    future.get();
                } catch (ExecutionException | InterruptedException _) {}
            }
        }

        executor.shutdown();
        return mergeBlocks(C, blockSize, q);
    }

    private boolean isNotSquare(int[][] matrix) {
        return matrix.length == 0 || matrix.length != matrix[0].length;
    }

    private int[][][][] splitMatrix(int[][] matrix, int size, int q) {
        var blocks = new int[q][q][size][size];

        for (var bi = 0; bi < q; bi++) {
            for (var bj = 0; bj < q; bj++) {
                for (var i = 0; i < size; i++) {
                    for (var j = 0; j < size; j++) {
                        var row = bi * size + i;
                        var col = bj * size + j;
                        blocks[bi][bj][i][j] = matrix[row][col];
                    }
                }
            }
        }

        return blocks;
    }

    private int[][] mergeBlocks(int[][][][] blocks, int size, int q) {
        var n = size * q;
        var result = new int[n][n];

        for (var bi = 0; bi < q; bi++) {
            for (var bj = 0; bj < q; bj++) {
                for (var i = 0; i < size; i++) {
                    for (var j = 0; j < size; j++) {
                        var row = bi * size + i;
                        var col = bj * size + j;
                        result[row][col] = blocks[bi][bj][i][j];
                    }
                }
            }
        }

        return result;
    }

    private int[][] add(int[][] a, int[][] b) {
        var n = a.length;
        var r = new int[n][n];

        for (var i = 0; i < n; i++) {
            for (var j = 0; j < n; j++) {
                r[i][j] = a[i][j] + b[i][j];
            }
        }

        return r;
    }

    @Override
    public int[][] multiplyForkJoin(int[][] a, int[][] b, int threadCount) {
        if (isNotSquare(a) || isNotSquare(b) || a.length != b.length) {
            throw new IllegalArgumentException("Matrices must be square and same size");
        }

        int n = a.length;
        int q = (int) Math.sqrt(threadCount);

        if (q * q != threadCount) {
            throw new IllegalArgumentException("Thread count must be perfect square");
        }

        if (n % q != 0) {
            throw new IllegalArgumentException("Matrix size must be divisible by sqrt(threadCount)");
        }

        int blockSize = n / q;

        int[][][][] A = splitMatrix(a, blockSize, q);
        int[][][][] B = splitMatrix(b, blockSize, q);
        int[][][][] C = new int[q][q][blockSize][blockSize];

        var pool = new ForkJoinPool(threadCount);
        pool.invoke(new FoxTask(A, B, C, q, blockSize));
        pool.shutdown();

        return mergeBlocks(C, blockSize, q);
    }

    private static class FoxTask extends RecursiveAction {

        private final int[][][][] A, B, C;
        private final int q, blockSize;

        FoxTask(int[][][][] A, int[][][][] B, int[][][][] C,
                int q, int blockSize) {
            this.A = A;
            this.B = B;
            this.C = C;
            this.q = q;
            this.blockSize = blockSize;
        }

        @Override
        protected void compute() {

            List<RecursiveAction> tasks = new ArrayList<>();

            for (int step = 0; step < q; step++) {

                for (int i = 0; i < q; i++) {
                    int shift = (i + step) % q;

                    for (int j = 0; j < q; j++) {

                        int fi = i;
                        int fj = j;
                        int fShift = shift;

                        tasks.add(new BlockMultiplyTask(
                                A[fi][fShift],
                                B[fShift][fj],
                                C[fi][fj],
                                blockSize
                        ));
                    }
                }
            }

            invokeAll(tasks);
        }
    }

    private static class BlockMultiplyTask extends RecursiveAction {

        private final int[][] A, B, C;
        private final int size;

        BlockMultiplyTask(int[][] A, int[][] B, int[][] C, int size) {
            this.A = A;
            this.B = B;
            this.C = C;
            this.size = size;
        }

        @Override
        protected void compute() {
            for (int i = 0; i < size; i++) {
                for (int k = 0; k < size; k++) {
                    int aVal = A[i][k];
                    for (int j = 0; j < size; j++) {
                        C[i][j] += aVal * B[k][j];
                    }
                }
            }
        }
    }

    public int[][] multiplyBasic(int[][] a, int[][] b, int threadCount) throws InterruptedException {
        if (threadCount < 1) {
            throw new InterruptedException("Кількість потоків не може бути менше одного");
        }

        var n = a.length;
        var rowsPerThread = n / threadCount;
        var extraRows = n % threadCount;

        if (rowsPerThread < 1) {
            throw new InterruptedException("Кількість потоків не може перевищувати розмірність матриці");
        }

        var result = new int[n][n];
        var executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        var startRow = 0;
        for (var t = 0; t < threadCount; t++) {
            var currentRows = rowsPerThread;
            if (t < extraRows) {
                currentRows++;
            }

            final var start = startRow;
            final var end = startRow + currentRows;

            var future = executor.submit(() -> {
                for (var i = start; i < end; i++) {
                    for (var j = 0; j < n; j++) {
                        var sum = 0;
                        for (var k = 0; k < n; k++) {
                            sum += a[i][k] * b[k][j];
                        }
                        result[i][j] = sum;
                    }
                }
            });

            futures.add(future);
            startRow = end;
        }

        for (var future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw new InterruptedException("Помилка виконання задачі: " + e.getCause().getMessage());
            }
        }

        executor.shutdown();
        return result;
    }
}
