package lib;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BasicMultiplicator implements IMultiThreadMatrixMultiplicator {
    @Override
    public int[][] multiply(int[][] a, int[][] b, int threadCount) throws InterruptedException {
        if (threadCount < 1) {
            throw new InterruptedException("Кількість потоків не може бути менше одного");
        }

        var n = a.length;
        var rowsPerThread = n / threadCount;
        var extraRows = n % threadCount;

        if (rowsPerThread < 1) {
            throw new InterruptedException("Кількість потоків не може перевищувати розмірність матриці");
        }

        var result = new int[n][n];
        var executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        var startRow = 0;
        for (var t = 0; t < threadCount; t++) {
            var currentRows = rowsPerThread;
            if (t < extraRows) {
                currentRows++;
            }

            final var start = startRow;
            final var end = startRow + currentRows;

            var future = executor.submit(() -> {
                for (var i = start; i < end; i++) {
                    for (var j = 0; j < n; j++) {
                        var sum = 0;
                        for (var k = 0; k < n; k++) {
                            sum += a[i][k] * b[k][j];
                        }
                        result[i][j] = sum;
                    }
                }
            });

            futures.add(future);
            startRow = end;
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