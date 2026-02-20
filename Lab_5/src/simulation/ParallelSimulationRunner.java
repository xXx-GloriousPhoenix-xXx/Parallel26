package simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelSimulationRunner {
    private final int numRuns;
    private final int numServers;
    private final int queueCapacity;
    private final double arrivalRate;
    private final double serviceRate;
    private final int arrivalsPerRun;
    private final int parallelThreads;
    private final boolean monitorEnabled;

    public ParallelSimulationRunner(int numRuns, int numServers, int queueCapacity,
                                    double arrivalRate, double serviceRate,
                                    int arrivalsPerRun, int parallelThreads,
                                    boolean monitorEnabled) {
        this.numRuns = numRuns;
        this.numServers = numServers;
        this.queueCapacity = queueCapacity;
        this.arrivalRate = arrivalRate;
        this.serviceRate = serviceRate;
        this.arrivalsPerRun = arrivalsPerRun;
        this.parallelThreads = parallelThreads;
        this.monitorEnabled = monitorEnabled;
    }

    public void run() throws InterruptedException, ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(parallelThreads);
        List<Future<SimulationResult>> futures = new ArrayList<>();

        for (int run = 0; run < numRuns; run++) {
            Long seed = (long) run;
            QueueSimulation sim = new QueueSimulation(
                    numServers, queueCapacity, arrivalRate, serviceRate,
                    arrivalsPerRun, seed, monitorEnabled, run + 1);
            futures.add(pool.submit(sim));
        }

        List<SimulationResult> results = new ArrayList<>();
        for (Future<SimulationResult> future : futures) {
            results.add(future.get());
        }

        pool.shutdown();
        if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
            pool.shutdownNow();
        }

        int n = results.size();
        double sumQueue = 0.0;
        double sumReject = 0.0;
        for (SimulationResult res : results) {
            sumQueue += res.avgQueueLength();
            sumReject += res.rejectionProb();
        }
        double meanQueue = sumQueue / n;
        double meanReject = sumReject / n;

        double varQueue = 0.0, varReject = 0.0;
        for (SimulationResult res : results) {
            varQueue += Math.pow(res.avgQueueLength() - meanQueue, 2);
            varReject += Math.pow(res.rejectionProb() - meanReject, 2);
        }
        double stdQueue = Math.sqrt(varQueue / (n - 1));
        double stdReject = Math.sqrt(varReject / (n - 1));

        double tValue = getTValue(n - 1);
        double ciQueue = tValue * stdQueue / Math.sqrt(n);
        double ciReject = tValue * stdReject / Math.sqrt(n);

        System.out.printf("Кількість прогонів: %d\n", n);
        System.out.format("\nДовжина черги\nСередня: %.3f ± %.3f (95%%)\nВідхилення: %.3f\n", meanQueue, ciQueue, stdQueue);
        System.out.format("\nЙмовірність відмови\nСередня: %.3f ± %.3f (95%%)\nВідхилення: %.3f\n", meanReject, ciReject, stdReject);
    }

    private double getTValue(int df) {
        if (df <= 0) return 1.96;
        if (df == 1) return 12.706;
        if (df == 2) return 4.303;
        if (df == 3) return 3.182;
        if (df == 4) return 2.776;
        if (df == 5) return 2.571;
        if (df == 6) return 2.447;
        if (df == 7) return 2.365;
        if (df == 8) return 2.306;
        if (df == 9) return 2.262;
        if (df == 10) return 2.228;
        if (df <= 20) return 2.086;
        if (df <= 30) return 2.042;
        return 1.96;
    }
}
