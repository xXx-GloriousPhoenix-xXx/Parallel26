package factory;

import java.util.Random;

public class MatrixGenerator {
    public int[][] generate(int size, int min, int max) {
        var random = new Random();
        var matrix = new int[size][size];
        for (var i = 0; i < size; i++) {
            for (var j = 0; j < size; j++) {
                matrix[i][j] = random.nextInt(min, max);
            }
        }
        return matrix;
    }
}
