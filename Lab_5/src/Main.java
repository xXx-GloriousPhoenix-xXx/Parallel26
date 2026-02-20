import simulation.ParallelSimulationRunner;
import simulation.QueueSimulation;

void main() throws Exception {
    int servers = 3;
    int capacity = 5;
    double lambda = 5.0;
    double mu = 3.0;
    int arrivalsPerRun = (int)1e6;

    runSingle(servers, capacity, lambda, mu, arrivalsPerRun);

    runParallel(servers, capacity, lambda, mu, arrivalsPerRun);
}

void runSingle(int servers, int capacity, double lambda, double mu, int arrivalsPerRun) throws Exception {
    System.out.println("=== Запуск одного прогону з динамічним виведенням стану ===");
    var singleRun = new QueueSimulation(
            servers, capacity, lambda, mu, arrivalsPerRun,
            42L,
            false,
            1);
    var result = singleRun.call();
    System.out.printf("Середня довжина черги: %.3f\n", result.avgQueueLength());
    System.out.printf("Ймовірність відмови: %.3f\n", result.rejectionProb());
    System.out.printf("Всього заявок: %d\nВідмов: %d\n", result.totalArrivals(), result.totalRejected());
}

void runParallel(int servers, int capacity, double lambda, double mu, int arrivalsPerRun) throws ExecutionException, InterruptedException {
    System.out.println("\n=== Запуск паралельних прогонів ===");
    int numRuns = 30;
    int parallelThreads = Runtime.getRuntime().availableProcessors();

    var runner = new ParallelSimulationRunner(
            numRuns, servers, capacity, lambda, mu, arrivalsPerRun,
            parallelThreads,
            false);
    runner.run();
}