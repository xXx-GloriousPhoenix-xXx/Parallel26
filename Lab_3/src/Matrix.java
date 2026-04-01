import java.util.Random;

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
        var bBlocks = splitIntoBlocksTransposed(other.matrix, q, blockSize);
        var cBlocks = new int[q][q][blockSize][blockSize];

        var threads = new Thread[threadCount];
        var threadIndex = 0;

        for (var i = 0; i < q; i++) {
            for (var j = 0; j < q; j++) {
                final var row = i;
                final var col = j;

                threads[threadIndex] = new Thread(() -> {
                    var currentABlock = aBlocks[row][col];
                    var currentBBlock = bBlocks[row][col];
                    var resultBlock = new int[blockSize][blockSize];

                    for (var k = 0; k < q; k++) {
                        var leadingCol = (row + k) % q;
                        var aBlockToMultiply = getABlockForStep(aBlocks, row, leadingCol, k, q);
                        multiplyBlocks(aBlockToMultiply, currentBBlock, resultBlock);
                        currentBBlock = getBBlockForNextStep(bBlocks, row, col, k, q);
                    }

                    cBlocks[row][col] = resultBlock;
                });

                threads[threadIndex].start();
                threadIndex++;
            }
        }

        for (var t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
        }

        var result = mergeBlocks(cBlocks, q, blockSize);
        return new Matrix(result);
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
    private int[][][][] splitIntoBlocksTransposed(int[][] matrix, int q, int blockSize) {
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
    private int[][] getABlockForStep(int[][][][] aBlocks, int row, int leadingCol, int step, int q) {
        return aBlocks[row][leadingCol];
    }
    private int[][] getBBlockForNextStep(int[][][][] bBlocks, int row, int col, int step, int q) {
        var nextRow = (row - 1 + q) % q;
        return bBlocks[nextRow][col];
    }
    private void multiplyBlocks(int[][] aBlock, int[][] bBlock, int[][] resultBlock) {
        var size = aBlock.length;

        for (var i = 0; i < size; i++) {
            for (var j = 0; j < size; j++) {
                var sum = 0;
                for (var k = 0; k < size; k++) {
                    sum += aBlock[i][k] * bBlock[k][j];
                }
                resultBlock[i][j] += sum;
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
