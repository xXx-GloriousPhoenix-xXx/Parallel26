package test;

import factory.MatrixGenerator;
import lib.BasicMultiplicator;
import lib.FoxMultiplicator;
import lib.RibbonMultiplicator;

import java.util.*;

public class PerformanceTest {
//    private static final int[] MATRIX_SIZES = {500, 1000, 1500, 2000, 2500, 3000};
    private static final int[] MATRIX_SIZES = { 400 };

    private static final int[] THREAD_COUNTS = {4, 16, 25};

    private static final int RUNS_PER_TEST = 20;

    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 100;

    static void main() throws Exception {
        var matrixGenerator = new MatrixGenerator();
        var basicMultiplicator = new BasicMultiplicator();
        var ribbonMultiplicator = new RibbonMultiplicator();
        var foxMultiplicator = new FoxMultiplicator();

        testParallelMultiplication("ЗВИЧАЙНЕ МНОЖЕННЯ",
                (size, threads) -> {
                    try {
                        var a = matrixGenerator.generate(size, MIN_VALUE, MAX_VALUE);
                        var b = matrixGenerator.generate(size, MIN_VALUE, MAX_VALUE);
                        return basicMultiplicator.multiply(a, b, threads);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

        testParallelMultiplication("СТРІЧКОВЕ МНОЖЕННЯ",
                (size, threads) -> {
                    try {
                        var a = matrixGenerator.generate(size, MIN_VALUE, MAX_VALUE);
                        var b = matrixGenerator.generate(size, MIN_VALUE, MAX_VALUE);
                        return ribbonMultiplicator.multiply(a, b, threads);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

        testParallelMultiplication("МНОЖЕННЯ ФОКСА",
                (size, threads) -> {
                    try {
                        var a = matrixGenerator.generate(size, MIN_VALUE, MAX_VALUE);
                        var b = matrixGenerator.generate(size, MIN_VALUE, MAX_VALUE);
                        return foxMultiplicator.multiply(a, b, threads);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static void testParallelMultiplication(String methodName,
                                                   MultiplicationFunction function) {
        System.out.println("\n" + methodName);
        System.out.println("=".repeat(105));

        for (int threads : THREAD_COUNTS) {
            System.out.printf("\nКількість потоків: %d%n", threads);
            System.out.println("-".repeat(105));
            System.out.printf("%-10s %-20s %-20s %-20s %-15s %-15s%n",
                    "Розмір", "Мін. час (мс)", "Макс. час (мс)", "Сер. час (мс)", "Стд. відхил.", "Прискорення");
            System.out.println("-".repeat(105));

            Map<Integer, Double> sequentialTimes = new HashMap<>();

            for (int size : MATRIX_SIZES) {
                if (!canUseThreads(size, threads, methodName)) {
                    System.out.printf("%-12d %-70s%n", size, "Неможливо виконати (матриця не ділиться на потоки)");
                    continue;
                }

                List<Long> times = new ArrayList<>();

                for (int run = 0; run < RUNS_PER_TEST; run++) {
                    try {
                        long start = System.nanoTime();
                        function.multiply(size, threads);
                        long end = System.nanoTime();

                        times.add((end - start) / 1_000_000);
                    } catch (Exception e) {
                        System.err.println("Помилка під час виконання: " + e.getMessage());
                    }
                }

                if (!sequentialTimes.containsKey(size)) {
                    sequentialTimes.put(size, getSequentialTime(size));
                }

                printParallelStatistics(size, threads, times, sequentialTimes.get(size));
            }
        }
    }

    private static boolean canUseThreads(int size, int threads, String methodName) {
        if (methodName.contains("ФОКСА")) {
            int q = (int) Math.sqrt(threads);
            return q * q == threads && size % q == 0;
        } else {
            return threads <= size;
        }
    }

    private static double getSequentialTime(int size) {
        return size * size * size / 1_000_000.0;
    }

    private static void printParallelStatistics(int size, int threads, List<Long> times, double sequentialTime) {
        if (times.isEmpty()) {
            System.out.printf("%-12d %-15s%n", size, "Помилка");
            return;
        }

        DoubleSummaryStatistics stats = times.stream()
                .mapToDouble(Long::doubleValue)
                .summaryStatistics();

        double avg = stats.getAverage();
        double min = stats.getMin();
        double max = stats.getMax();
        double stdDev = calculateStdDev(times, avg);
        double speedup = sequentialTime / avg;

        System.out.printf("%-10s %-20.2f %-20.2f %-20.2f %-15.2f %-15.2f%n",
                size, avg, min, max, stdDev, speedup);
    }

    private static double calculateStdDev(List<Long> times, double mean) {
        double sum = 0;
        for (long time : times) {
            sum += Math.pow(time - mean, 2);
        }
        return Math.sqrt(sum / times.size());
    }

    @FunctionalInterface
    interface MultiplicationFunction {
        int[][] multiply(int size, int threads) throws Exception;
    }
}