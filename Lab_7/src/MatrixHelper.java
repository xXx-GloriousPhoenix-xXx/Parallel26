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
}
