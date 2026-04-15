package multiplicator;

import java.util.Random;

public class Helper {
    public static int[][] generate(int n, int min, int max) {
        var random = new Random();
        var matrix = new int[n][n];
        for (var i = 0; i < n; i++) {
            for (var j = 0; j < n; j++) {
                matrix[i][j] = random.nextInt(min, max);
            }
        }
        return matrix;
    }
    public static int[][] multiplyPartial(int[][] a, int startInclusive, int endExclusive, int[][] b) {
        var k = a[0].length;
        var m = b[0].length;

        if (b.length != k) {
            throw new IllegalArgumentException("Incompatible matrix sizes");
        }

        var rows = endExclusive - startInclusive;
        var result = new int[rows][m];

        for (var i = startInclusive; i < endExclusive; i++) {
            for (var j = 0; j < m; j++) {
                var sum = 0;
                for (var t = 0; t < k; t++) {
                    sum += a[i][t] * b[t][j];
                }
                result[i - startInclusive][j] = sum;
            }
        }

        return result;
    }
    public static int[] flatten(int[][] m, int startRow, int rowCount, int rowSize) {
        var flat = new int[rowCount * rowSize];
        for (var i = 0; i < rowCount; i++) {
            for (var j = 0; j < rowSize; j++) {
                var index = i * rowSize + j;
                flat[index] = m[startRow + i][j];
            }
        }
        return flat;
    }
    public static int[][] unflatten(int[] a, int rowCount, int rowSize) {
        var unflat = new int[rowCount][rowSize];
        for (var i = 0; i < rowCount; i++) {
            for (var j = 0; j < rowSize; j++) {
                var index = i * rowSize + j;
                unflat[i][j] = a[index];
            }
        }
        return unflat;
    }
    public static void display(int[][] m, boolean addSpace) {
        var max = Integer.MIN_VALUE;
        for (var row : m) {
            for (var col : row) {
                max = Math.max(max, col);
            }
        }

        var len = String.valueOf(max).length() + 1;

        for (var row : m) {
            for (var col : row) {
                System.out.printf("%" + len + "d", col);
            }
            System.out.println();
        }

        if (addSpace) {
            System.out.println(" ");
        }

        System.out.flush();
    }
}
