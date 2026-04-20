package org.example.lab8.lab_8_solution.controller;

import java.util.Random;
import java.util.stream.IntStream;

public class MatrixService {
    public static int[][] multiplyParallel(int[][] a, int[][] b) {
        var n = a.length;
        var c = new int[n][n];

        IntStream.range(0, n)
                .parallel()
                .forEach(i -> {
                    for (var j = 0; j < n; j++) {
                        var sum = 0;
                        for (var k = 0; k < n; k++) {
                            sum += a[i][k] * b[k][j];
                        }
                        c[i][j] = sum;
                    }
                });

        return c;
    }
    public static int[][] generateMatrix(int n, int min, int max) {
        var random = new Random();
        var matrix = new int[n][n];
        for (var i = 0; i < n; i++) {
            for (var j = 0; j < n; j++) {
                matrix[i][j] = random.nextInt(min, max);
            }
        }
        return matrix;
    }
}