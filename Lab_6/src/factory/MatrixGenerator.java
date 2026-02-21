package factory;

import java.util.Random;

public class MatrixGenerator {
    static Random random = new Random();
    public static int[][] generateSquareMatrix(int size, int min, int max) {
        var result = new int[size][size];
        for (var i = 0; i < size; i++) {
            for (var j = 0; j < size; j++) {
                result[i][j] = random.nextInt(min, max);
            }
        }
        return result;
    }
}
