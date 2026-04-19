package multiplicator;

import mpi.MPI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Collective implements ILab7 {
    private static final String SAVE_PATH = "F:\\Programmes\\Github\\Reps\\Parallel26\\Lab_7\\src\\utils\\collective.csv";

    public static void main(String[] args) {
        MPI.Init(args);

        var size = 16;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--") && i + 1 < args.length) {
                size = Integer.parseInt(args[i + 1]);
                break;
            }
        }

        var rank = MPI.COMM_WORLD.Rank();
        var workerCount = MPI.COMM_WORLD.Size();
        var rowsPerWorker = size / workerCount;

        if (rank == MASTER) {
            System.out.printf("Type: Collective, Size: %d, Workers: %d%n", size, workerCount);
            System.out.printf("Warming up (%d runs)...%n", WARMUP);
        }

        for (var i = 0; i < WARMUP; i++) {
            runOnce(rank, size, workerCount, rowsPerWorker);
        }

        if (rank == MASTER) {
            System.out.println("Measuring...");
        }

        var times = new long[RUNS];
        for (var run = 0; run < RUNS; run++) {
            times[run] = runOnce(rank, size, workerCount, rowsPerWorker);
            if (rank == MASTER) {
                System.out.printf("   Time: %.3f ms%n", times[run] / 1_000_000.0);
            }
        }

        if (rank == MASTER) {
            Helper.saveResults(times, size, workerCount, SAVE_PATH);
        }

        MPI.Finalize();
    }

    private static long runOnce(int rank, int size, int workerCount, int rowsPerWorker) {
        var flatA = new int[size * size];
        var flatB = new int[size * size];
        var localFlatA = new int[rowsPerWorker * size];
        var localFlatC = new int[rowsPerWorker * size];

        if (rank == MASTER) {
            var a = Helper.generate(size, MIN, MAX);
            var b = Helper.generate(size, MIN, MAX);
            flatA = Helper.flatten(a, 0, size, size);
            flatB = Helper.flatten(b, 0, size, size);
        }

        MPI.COMM_WORLD.Barrier();
        var start = 0L;
        if (rank == MASTER) {
            start = System.nanoTime();
        }

        MPI.COMM_WORLD.Bcast(flatB, 0, size * size, MPI.INT, MASTER);

        MPI.COMM_WORLD.Scatter(
                flatA, 0, rowsPerWorker * size, MPI.INT,
                localFlatA, 0, rowsPerWorker * size, MPI.INT,
                MASTER
        );

        var localA = Helper.unflatten(localFlatA, rowsPerWorker, size);
        var localB = Helper.unflatten(flatB, size, size);
        var localC = Helper.multiplyPartial(localA, 0, rowsPerWorker, localB);
        localFlatC = Helper.flatten(localC, 0, rowsPerWorker, size);

        var flatC = new int[size * size];
        MPI.COMM_WORLD.Gather(
                localFlatC, 0, rowsPerWorker * size, MPI.INT,
                flatC, 0, rowsPerWorker * size, MPI.INT,
                MASTER
        );

        MPI.COMM_WORLD.Barrier();
        if (rank == MASTER) {
            return System.nanoTime() - start;
        }
        return 0L;
    }
}