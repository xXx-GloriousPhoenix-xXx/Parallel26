void main() throws Exception {
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
        } catch (InterruptedException ignored) {
        }
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

    IO.println("\n=== THEORETICAL ===");
    IO.println("Theoretical Avg queue length: " + theoretical.avgQueueLength);
    IO.println("Theoretical Rejection probability: " + theoretical.rejectionProbability);

    IO.println("\n=== ACTUAL ===");
    IO.println("Avg queue length: " + totalQueue / runs);
    IO.println("Rejection probability: " + totalReject / runs);
}