import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Matrix {
    public final int[][] matrix;
    public Matrix() {
        matrix = new int[1][1];
    }
    public Matrix(int[][] matrix) {
        this.matrix = matrix;
    }
    public Matrix(int size, int min, int max) {
        var random = new Random();
        var matrix = new int[size][size];
        for (var i = 0; i < size; i++) {
            for (var j = 0; j < size; j++) {
                matrix[i][j] = random.nextInt(min, max + 1);
            }
        }
        this.matrix = matrix;
    }
    public void display() {
        var max = Integer.MIN_VALUE;
        for (var row : matrix) {
            for (var col : row) {
                max = Math.max(max, col);
            }
        }

        var len = String.valueOf(max).length() + 1;

        for (var row : matrix) {
            for (var col : row) {
                System.out.printf("%" + len + "d", col);
            }
            System.out.println();
        }
    }
    public Boolean isMultiplyable(Matrix other) {
        return this.matrix.length == other.matrix[0].length;
    }
    public Boolean isSquare() {
        return this.matrix.length == this.matrix[0].length;
    }
    public Boolean isEqual(Matrix other) {
        var a = this.matrix;
        var b = other.matrix;

        var aRows = a.length;
        var bRows = b.length;
        var aCols = a[0].length;
        var bCols = b[0].length;

        if (aRows != bRows || aCols != bCols) {
            return false;
        }

        for (var i = 0; i < aRows; i++) {
            for (var j = 0; j < aCols; j++) {
                if (a[i][j] != b[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }
    public Matrix multiplySequential(Matrix other) {
        if (!isMultiplyable(other)) {
            throw new IllegalArgumentException("Matrices are not multiplyable");
        }

        var a = this.matrix;
        var b = other.matrix;

        var aRows = a.length;
        var bCols = b[0].length;
        var commonSize = a[0].length;

        var c = new int[aRows][bCols];
        for (var i = 0; i < aRows; i++) {
            for (var j = 0; j < commonSize; j++) {
                var sum = 0;
                for (var k = 0; k < bCols; k++) {
                    sum += a[i][k] * b[k][j];
                }
                c[i][j] = sum;
            }
        }

        return new Matrix(c);
    }
    public Matrix multiplyRibbon(Matrix other, int threadCount) {
        if (!isMultiplyable(other)) {
            throw new IllegalArgumentException("Matrices are not multiplyable");
        }

        var a = this.matrix;
        var b = other.matrix;

        var aRows = a.length;
        var commonSize = a[0].length;
        var bCols = b[0].length;

        var rowsPerThread = aRows / threadCount;
        var remainingRows = aRows % threadCount;

        var threads = new Thread[threadCount];
        var startRow = 0;

        var c = new int[aRows][bCols];
        for (var t = 0; t < threadCount; t++) {
            var rowsToProcess = rowsPerThread + (t < remainingRows ? 1 : 0);
            var endRow = startRow + rowsToProcess;

            final var threadStartRow = startRow;
            final var threadEndRow = endRow;

            threads[t] = new Thread(() -> {
                for (var i = threadStartRow; i < threadEndRow; i++) {
                    for (var j = 0; j < commonSize; j++) {
                        var sum = 0;
                        for (var k = 0; k < bCols; k++) {
                            sum += a[i][k] * b[k][j];
                        }
                        c[i][j] = sum;
                    }
                }
            });

            threads[t].start();
            startRow = endRow;
        }

        for (var t : threads) {
            try {
                t.join();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
        }

        return new Matrix(c);
    }
    public Matrix multiplyFox(Matrix other, int threadCount) {
        if (!isMultiplyable(other)) {
            throw new IllegalArgumentException("Matrices are not multiplyable");
        }
        if (!this.isSquare() || !other.isSquare()) {
            throw new IllegalArgumentException("Matrices must be square");
        }

        var q = (int) Math.sqrt(threadCount);
        if (q * q != threadCount) {
            throw new IllegalArgumentException("Thread count must be a perfect square");
        }

        var size = this.matrix.length;
        if (size % q != 0) {
            throw new IllegalArgumentException("Matrix size must be divisible by sqrt(threadCount)");
        }

        var blockSize = size / q;
        var aBlocks = splitIntoBlocks(this.matrix, q, blockSize);
        var localB   = splitIntoBlocks(other.matrix, q, blockSize);
        var cBlocks  = new int[q][q][blockSize][blockSize];

        var barrier1 = new CyclicBarrier(threadCount);
        var barrier2 = new CyclicBarrier(threadCount);

        var nextB = new int[q][q][][];

        var threads = new Thread[threadCount];
        var exceptions = new RuntimeException[1];

        for (var i = 0; i < q; i++) {
            for (var j = 0; j < q; j++) {
                final var row = i;
                final var col = j;

                threads[row * q + col] = new Thread(() -> {
                    try {
                        for (var step = 0; step < q; step++) {
                            var pivotCol = (row + step) % q;
                            var aBlock = aBlocks[row][pivotCol];
                            multiplyBlocks(aBlock, localB[row][col], cBlocks[row][col]);
                            barrier1.await();

                            var destRow = (row - 1 + q) % q;
                            nextB[destRow][col] = localB[row][col];
                            barrier2.await();

                            localB[row][col] = nextB[row][col];
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        exceptions[0] = new RuntimeException("Thread interrupted", e);
                    } catch (BrokenBarrierException e) {
                        exceptions[0] = new RuntimeException("Barrier broken", e);
                    }
                });
            }
        }

        for (var t : threads) t.start();
        for (var t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
        }

        if (exceptions[0] != null) throw exceptions[0];

        return new Matrix(mergeBlocks(cBlocks, q, blockSize));
    }
    private int[][][][] splitIntoBlocks(int[][] matrix, int q, int blockSize) {
        var blocks = new int[q][q][blockSize][blockSize];
        for (var i = 0; i < q; i++) {
            for (var j = 0; j < q; j++) {
                for (var bi = 0; bi < blockSize; bi++) {
                    for (var bj = 0; bj < blockSize; bj++) {
                        blocks[i][j][bi][bj] = matrix[i * blockSize + bi][j * blockSize + bj];
                    }
                }
            }
        }
        return blocks;
    }
    private void multiplyBlocks(int[][] a, int[][] b, int[][] result) {
        var size = a.length;
        for (var i = 0; i < size; i++) {
            for (var k = 0; k < size; k++) {
                if (a[i][k] == 0) continue;
                for (var j = 0; j < size; j++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
    }
    private int[][] mergeBlocks(int[][][][] blocks, int q, int blockSize) {
        var result = new int[q * blockSize][q * blockSize];
        for (var i = 0; i < q; i++) {
            for (var j = 0; j < q; j++) {
                for (var bi = 0; bi < blockSize; bi++) {
                    for (var bj = 0; bj < blockSize; bj++) {
                        result[i * blockSize + bi][j * blockSize + bj] = blocks[i][j][bi][bj];
                    }
                }
            }
        }
        return result;
    }
}
