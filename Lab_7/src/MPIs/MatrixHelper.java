package MPIs;

import java.util.Random;

public class MatrixHelper {
    private static final Random random = new Random();
    public static int[][] generate(int size, int min, int max) {
        var result = new int[size][size];
        for (var i = 0; i < size; i++) {
            for (var j = 0; j < size; j++) {
                result[i][j] = random.nextInt(min, max);
            }
        }
        return result;
    }
    public static void print(int[][] matrix) {
        for (var row : matrix) {
            for (var col : row) {
                System.out.printf("%d ", col);
            }
            System.out.println();
        }
    }

    public static int[] flatten(int[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        int[] flat = new int[rows * cols];
        for (int i = 0; i < rows; i++)
            System.arraycopy(matrix[i], 0, flat, i * cols, cols);
        return flat;
    }

    public static int[][] unflatten(int[] flat, int rows, int cols) {
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++)
            System.arraycopy(flat, i * cols, matrix[i], 0, cols);
        return matrix;
    }

    public static int[][] multiply(int[][] a, int[][] b) {
        var aRow = a.length;
        var aCol = a[0].length;
        var bCol = b[0].length;
        var result = new int[aRow][bCol];
        for (var i = 0; i < aRow; i++) {
            for (var j = 0; j < bCol; j++) {
                for (var k = 0; k < aCol; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }
}
