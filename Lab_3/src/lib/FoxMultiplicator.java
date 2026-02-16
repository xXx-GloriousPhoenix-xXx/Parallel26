package lib;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FoxMultiplicator implements IMultiThreadMatrixMultiplicator {
    @Override
    public int[][] multiply(int[][] a, int[][] b, int threadCount) throws InterruptedException {
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

        var basic = new BasicMultiplicator();

        var executor = Executors.newFixedThreadPool(threadCount);

        // Алгоритм Фокса
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
                            product = basic.multiply(A[fi][fShift], B[fShift][fJ], 1);
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
                } catch (ExecutionException _) {}
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
}