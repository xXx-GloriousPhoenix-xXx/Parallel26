package lib;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RibbonMultiplicator implements IMultiThreadMatrixMultiplicator {
    @Override
    public int[][] multiply(int[][] a, int[][] b, int threadCount) throws InterruptedException {
        if (threadCount < 1) {
            throw new InterruptedException("Кількість потоків не може бути менше одного");
        }

        int n = a.length;

        if (n != a[0].length || n != b.length || n != b[0].length) {
            throw new IllegalArgumentException("Матриці мають бути квадратними");
        }

        var result = new int[n][n];
        var executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;

            Future<?> future = executor.submit(() -> {
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        if ((i + j) % threadCount == threadId) {
                            int sum = 0;
                            for (int k = 0; k < n; k++) {
                                sum += a[i][k] * b[k][j];
                            }
                            result[i][j] = sum;
                        }
                    }
                }
            });

            futures.add(future);
        }

        for (var future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw new InterruptedException("Помилка виконання задачі: " + e.getCause().getMessage());
            }
        }

        executor.shutdown();
        return result;
    }
}