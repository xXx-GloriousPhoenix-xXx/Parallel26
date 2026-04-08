package matrix.multiplication;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MatrixMultiplicator implements IMatrixMultiplicator {
    // Sequential
    @Override
    public int[][] multiplySequential(int[][] a, int[][] b) {
        var n = a.length;
        var m = b[0].length;
        var k = b.length;

        var result = new int[n][m];

        for (var i = 0; i < n; i++) {
            for (var j = 0; j < m; j++) {
                var sum = 0;
                for (var t = 0; t < k; t++) {
                    sum += a[i][t] * b[t][j];
                }
                result[i][j] = sum;
            }
        }

        return result;
    }

    // ForkJoin
    @Override
    public int[][] multiplyForkJoin(int[][] a, int[][] b) {
        var n = a.length;
        var m = b[0].length;

        var result = new int[n][m];

        var pool = new ForkJoinPool();

        pool.invoke(new MultiplyTask(a, b, result, 0, n));

        pool.shutdown();

        return result;
    }

    // Task
    static class MultiplyTask extends RecursiveAction {
        private static final int THRESHOLD = 500;

        private final int[][] a;
        private final int[][] b;
        private final int[][] result;
        private final int startRow;
        private final int endRow;

        public MultiplyTask(int[][] a, int[][] b, int[][] result, int startRow, int endRow) {
            this.a = a;
            this.b = b;
            this.result = result;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        protected void compute() {
            if (endRow - startRow <= THRESHOLD) {
                computeDirect();
            } else {
                int mid = (startRow + endRow) / 2;

                MultiplyTask left = new MultiplyTask(a, b, result, startRow, mid);
                MultiplyTask right = new MultiplyTask(a, b, result, mid, endRow);

                invokeAll(left, right);
            }
        }

        private void computeDirect() {
            int m = b[0].length;
            int k = b.length;

            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < m; j++) {
                    int sum = 0;
                    for (int t = 0; t < k; t++) {
                        sum += a[i][t] * b[t][j];
                    }
                    result[i][j] = sum;
                }
            }
        }
    }
}