package simulation;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class QueueSimulation implements Callable<SimulationResult> {
    private final int numServers; // k
    private final int queueCapacity; // максимальна довжина черги
    private final double arrivalRate; // λ
    private final double serviceRate; // μ
    private final int numArrivals; // загальна кількість заявок для генерації
    private final Long seed;
    private final boolean monitorEnabled; // чи виводити стан під час моделювання
    private final int runId;

    private final BlockingQueue<Object> queue; // черга заявок
    private final AtomicLong totalArrivals = new AtomicLong(0);
    private final AtomicLong totalRejected = new AtomicLong(0);
    private final AtomicLong served = new AtomicLong(0);
    private volatile boolean noMoreArrivals = false;
    private volatile boolean simulationRunning = true;

    private long sumQueueLength = 0;
    private long sampleCount = 0;
    private static final int SAMPLE_INTERVAL_MS = 10;

    public QueueSimulation(int numServers, int queueCapacity,
                           double arrivalRate, double serviceRate,
                           int numArrivals, Long seed, boolean monitorEnabled, int runId) {
        this.numServers = numServers;
        this.queueCapacity = queueCapacity;
        this.arrivalRate = arrivalRate;
        this.serviceRate = serviceRate;
        this.numArrivals = numArrivals;
        this.seed = seed;
        this.monitorEnabled = monitorEnabled;
        this.runId = runId;
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
    }

    private double exponential(double mean) {
        var rng = (seed != null) ? new Random(seed) : ThreadLocalRandom.current();
        return -mean * Math.log(1 - rng.nextDouble());
    }

    @Override
    public SimulationResult call() throws Exception {
        var serverPool = Executors.newFixedThreadPool(numServers);

        for (var i = 0; i < numServers; i++) {
            serverPool.submit(this::serverTask);
        }

        var generatorThread = new Thread(this::generateArrivals);
        generatorThread.start();

        var samplingThread = new Thread(this::samplingTask);
        samplingThread.start();

        Thread monitorThread = null;
        if (monitorEnabled) {
            monitorThread = new Thread(this::monitorTask);
            monitorThread.start();
        }

        generatorThread.join();

        noMoreArrivals = true;

        serverPool.shutdown();
        if (!serverPool.awaitTermination(1, TimeUnit.MINUTES)) {
            serverPool.shutdownNow();
        }

        simulationRunning = false;
        samplingThread.interrupt();
        samplingThread.join();

        if (monitorThread != null) {
            monitorThread.interrupt();
            monitorThread.join();
        }

        var avgQueueLength = (sampleCount == 0) ? 0 : (double) sumQueueLength / sampleCount;
        var totalArr = totalArrivals.get();
        var totalRej = totalRejected.get();
        var rejectionProb = (totalArr == 0) ? 0 : (double) totalRej / totalArr;

        return new SimulationResult(avgQueueLength, rejectionProb, totalArr, totalRej);
    }

    private void generateArrivals() {
        for (var i = 0; i < numArrivals; i++) {
            var interArrival = exponential(1.0 / arrivalRate);
            try {
                Thread.sleep((long) interArrival);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            totalArrivals.incrementAndGet();
            if (!queue.offer(new Object())) {
                totalRejected.incrementAndGet(); // черга переповнена
            }
        }
    }

    private void serverTask() {
        while (!Thread.currentThread().isInterrupted()) {
            Object customer = null;
            try {
                customer = queue.poll(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (customer != null) {
                var serviceTime = exponential(1.0 / serviceRate);
                try {
                    Thread.sleep((long) serviceTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                served.incrementAndGet();
            } else {
                if (noMoreArrivals && queue.isEmpty()) {
                    break;
                }
            }
        }
    }

    private void samplingTask() {
        while (simulationRunning && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(SAMPLE_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            var currentSize = queue.size();
            sumQueueLength += currentSize;
            sampleCount++;
        }
    }

    private void monitorTask() {
        while (simulationRunning && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(200); // виводимо кожні 200 мс
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            int qSize = queue.size();
            long arr = totalArrivals.get();
            long rej = totalRejected.get();
            long serv = served.get();
            System.out.printf("[Прогін %d] Стан: черга=%d, Надійшло=%d, Обслуговано=%d, Відмов=%d, Ймовірність відмови=%.4f\n",
                    runId, qSize, arr, serv, rej, (arr == 0 ? 0 : (double) rej / arr));
        }
    }
}
