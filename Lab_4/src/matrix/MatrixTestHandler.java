package matrix;

import matrix.factory.MatrixGenerator;
import matrix.multiplication.FoxMultiplicator;

public class MatrixTestHandler {
    private static final int[] MATRIX_SIZES = { 1, 16, 81, 256, 625, 1296, 2401, 4096, 6561 };
    public static void handle() {
        System.out.format("%-10s | %-15s | %-15s | %-15s\n", "Розмір", "Класичний (мс)", "ForkJoin (мс)", "Прискорення");
        System.out.println("-".repeat(10) + " | " + "-".repeat(15) + " | " + "-".repeat(15) + " | " + "-".repeat(15));
        for (var size : MATRIX_SIZES) {
            handle(size);
        }
    }
    private static void handle(int size) {
        var generator = new MatrixGenerator();
        var matrix1 = generator.generate(size, 0, 100);
        var matrix2 = generator.generate(size, 0, 100);
        var multiplicator = new FoxMultiplicator();
        var threadCount = (int)Math.sqrt(size);

        var s1 = System.nanoTime();
        var classicResult = multiplicator.multiplyClassic(matrix1, matrix2, threadCount);
        var e1 = System.nanoTime();
        var t1 = (e1 - s1) / 1e6;

        var s2 = System.nanoTime();
        var forkJoinResult = multiplicator.multiplyForkJoin(matrix1, matrix2, threadCount);
        var e2 = System.nanoTime();
        var t2 = (e2 - s2) / 1e6;

        var speedup = t1 / t2;

        System.out.format("%-10d | %-15.3f | %-15.3f | %.3fx\n", size, t1, t2, speedup);
    }
}
