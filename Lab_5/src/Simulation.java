import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Simulation implements Callable<Result> {

    private final int channels;
    private final int queueCapacity;
    private final int totalRequests;
    private final double lambda;
    private final double mu;
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

        for (int i = 0; i < channels; i++) {
            servicePool.submit(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Integer req = queue.take();

                        // время обслуживания
                        Thread.sleep(expRandom(mu));

                        processed.incrementAndGet();
                    }
                } catch (InterruptedException ignored) {
                }
            });
        }

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