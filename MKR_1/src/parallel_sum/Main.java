package parallel_sum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Main {
    static void main() throws InterruptedException, ExecutionException {
        int n = 100, m = 5, h = 3;
        var array = new double[n];

        var rand = new Random();
        for (var i = 0; i < n; i++) {
            array[i] = rand.nextDouble() * 100;
        }

        var pool = Executors.newFixedThreadPool(h);
        List<Callable<Double>> tasks = new ArrayList<>();

        var size = (n + m - 1) / m;

        for (var i = 0; i < m; i++) {
            var start = i * size;
            var end = Math.min(start + size, n);

            tasks.add(() -> {
                var sum = 0.;
                for (var j = start; j < end; j++) {
                    sum += array[j];
                }
                return sum;
            });
        }

        var results = pool.invokeAll(tasks);

        double totalSum = 0;
        for (var f : results) {
            totalSum += f.get();
        }

        System.out.println("Сума = " + totalSum);

        pool.shutdown();
    }
}
