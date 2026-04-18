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

        var times = new long[RUNS];

        if (rank == MASTER) {
            System.out.printf("Type: Collective, Size: %d, Workers: %d%n", size, workerCount);
        }

        for (var run = 0; run < RUNS; run++) {
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

            if (rank == MASTER) {
                var c = Helper.unflatten(flatC, size, size);
            }

            MPI.COMM_WORLD.Barrier();
            if (rank == MASTER) {
                var time = System.nanoTime() - start;
                times[run] = time;
                System.out.printf("   Time: %.3f ms%n", time / 1_000_000.0);
            }
        }

        if (rank == MASTER) {
            var avg = Arrays
                    .stream(times)
                    .average()
                    .orElse(0)
                    / 1_000_000.0;
            System.out.printf("Avg: %.3f ms", avg);

            var file = new File(SAVE_PATH);
            var writeHeader = !file.exists() || file.length() == 0;
            try (var writer = new FileWriter(file, true)) {
                if (writeHeader) {
                    writer.write("size;avg_ms;workers\n");
                }
                writer.write(String.format("%d;%.3f;%d%n", size, avg, workerCount));
            } catch (IOException e) {
                System.err.println("CSV write error: " + e.getMessage());
            }
        }

        MPI.Finalize();
    }
}