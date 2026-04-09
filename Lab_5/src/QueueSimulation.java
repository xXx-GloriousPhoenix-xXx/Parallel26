import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class QueueSimulation {

    static class Result {
        double avgQueueLength;
        double rejectionProbability;

        public Result(double avgQueueLength, double rejectionProbability) {
            this.avgQueueLength = avgQueueLength;
            this.rejectionProbability = rejectionProbability;
        }
    }

    static class GlobalStats {
        AtomicLong totalQueueSum = new AtomicLong(0);
        AtomicLong totalQueueCount = new AtomicLong(0);
        AtomicInteger totalRejected = new AtomicInteger(0);
        AtomicInteger totalRequests = new AtomicInteger(0);
    }

    static class Simulation implements Callable<Result> {

        private final int channels;
        private final int queueCapacity;
        private final int totalRequests;
        private final double lambda; // интенсивность поступления
        private final double mu;     // интенсивность обслуживания
        private final GlobalStats stats;

        public Simulation(int channels, int queueCapacity, int totalRequests,
                          double lambda, double mu, GlobalStats stats) {
            this.channels = channels;
            this.queueCapacity = queueCapacity;
            this.totalRequests = totalRequests;
            this.lambda = lambda;
            this.mu = mu;
            this.stats = stats;
        }

        @Override
        public Result call() throws Exception {

            BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(queueCapacity);

            ExecutorService servicePool = Executors.newFixedThreadPool(channels);

            AtomicInteger rejected = new AtomicInteger(0);
            AtomicInteger processed = new AtomicInteger(0);

            List<Integer> queueSizes = Collections.synchronizedList(new ArrayList<>());

            // каналы обслуживания
            for (int i = 0; i < channels; i++) {
                servicePool.submit(() -> {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            Integer req = queue.take();

                            // время обслуживания
                            Thread.sleep(expRandom(mu));

                            processed.incrementAndGet();
                        }
                    } catch (InterruptedException ignored) {}
                });
            }

            // генерация заявок
            for (int i = 0; i < totalRequests; i++) {
                Thread.sleep(expRandom(lambda));

                if (!queue.offer(i)) {
                    rejected.incrementAndGet();
                    stats.totalRejected.incrementAndGet();
                }

                int size = queue.size();
                queueSizes.add(size);

                stats.totalQueueSum.addAndGet(size);
                stats.totalQueueCount.incrementAndGet();
                stats.totalRequests.incrementAndGet();
            }

            servicePool.shutdownNow();

            // расчет статистики
            double avgQueue = queueSizes.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0);

            double rejectionProb = (double) rejected.get() / totalRequests;

            return new Result(avgQueue, rejectionProb);
        }

        private long expRandom(double rate) {
            return (long) (-Math.log(1 - Math.random()) / rate * 100);
        }
    }

    static class TheoreticalModel {
        public static Result calculate(int c, int K, double lambda, double mu) {
            var maxState = c + K;
            var a = lambda / mu;
            var P = new double[maxState + 1];
            var sum = 0.0;
            for (var n = 0; n <= maxState; n++) {
                sum += stateProbabilityRaw(n, c, a);
            }
            var P0 = 1.0 / sum;
            for (var n = 0; n <= maxState; n++) {
                P[n] = stateProbabilityRaw(n, c, a) * P0;
            }
            var rejectProb = P[maxState];
            var Lq = 0.0;
            for (var n = c; n <= maxState; n++) {
                Lq += (n - c) * P[n];
            }
            return new Result(Lq, rejectProb);
        }

        private static double stateProbabilityRaw(int n, int c, double a) {
            if (n <= c) {
                return Math.pow(a, n) / factorial(n);
            } else {
                return Math.pow(a, n) / (factorial(c) * Math.pow(c, n - c));
            }
        }

        private static double factorial(int n) {
            var res = 1.0;
            for (var i = 2; i <= n; i++) res *= i;
            return res;
        }
    }

    public static void main(String[] args) throws Exception {
        var runs = 10;
        var simulationPool = Executors.newFixedThreadPool(4);
        List<Future<Result>> futures = new ArrayList<>();

        var stats = new GlobalStats();

        for (var i = 0; i < runs; i++) {
            futures.add(simulationPool.submit(
                    new Simulation(
                            3,   // кількість каналів
                            20,   // довжина черги
                            5000, // кількість заявок
                            30, // генерацій/сек
                            7,  // обробок/сек
                            stats // статистика
                    )
            ));
        }

        var monitor = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    var count = stats.totalQueueCount.get();
                    var avgQueue = count == 0 ? 0 : (double) stats.totalQueueSum.get() / count;
                    var totalReq = stats.totalRequests.get();
                    var rejectProb = totalReq == 0 ? 0 : (double) stats.totalRejected.get() / totalReq;
                    System.out.printf(
                            "LIVE: avgQueue=%.3f | rejectProb=%.3f%n",
                            avgQueue,
                            rejectProb
                    );
                    Thread.sleep(500);
                }
            } catch (InterruptedException ignored) {}
        });

        IO.println("=== START ===");

        monitor.start();

        var totalQueue = 0.;
        var totalReject = 0.;

        for (var f : futures) {
            Result r = f.get();
            totalQueue += r.avgQueueLength;
            totalReject += r.rejectionProbability;
        }

        monitor.interrupt();
        simulationPool.shutdown();

        var theoretical = TheoreticalModel.calculate(
                3,   // c
                20,  // K
                30,  // lambda
                7    // mu
        );

        System.out.println("\n=== THEORETICAL ===");
        System.out.println("Theoretical Avg queue length: " + theoretical.avgQueueLength);
        System.out.println("Theoretical Rejection probability: " + theoretical.rejectionProbability);

        System.out.println("\n=== ACTUAL ===");
        System.out.println("Avg queue length: " + totalQueue / runs);
        System.out.println("Rejection probability: " + totalReject / runs);
    }
}