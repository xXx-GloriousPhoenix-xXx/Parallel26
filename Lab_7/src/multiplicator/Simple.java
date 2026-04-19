package multiplicator;

import mpi.*;

public class Simple implements ILab7 {
    private static final int META_TAG = 0;
    private static final int A_TAG = 1;
    private static final int B_TAG = 2;
    private static final int C_TAG = 3;
    private static final String SAVE_PATH = "F:\\Programmes\\Github\\Reps\\Parallel26\\Lab_7\\src\\utils\\simple.csv";

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
        var rowsRemainder = size % workerCount;

        if (rank == MASTER) {
            System.out.printf("Type: Simple, Size: %d, Workers: %d%n", size, workerCount);
            System.out.printf("Warming up (%d runs)...%n", WARMUP);
        }

        for (var i = 0; i < WARMUP; i++) {
            runOnce(rank, size, workerCount, rowsPerWorker, rowsRemainder);
        }

        if (rank == MASTER) {
            System.out.println("Measuring...");
        }

        var times = new long[RUNS];
        for (var run = 0; run < RUNS; run++) {
            times[run] = runOnce(rank, size, workerCount, rowsPerWorker, rowsRemainder);
            if (rank == MASTER) {
                System.out.printf("   Time: %.3f ms%n", times[run] / 1_000_000.0);
            }
        }

        if (rank == MASTER) {
            Helper.saveResults(times, size, workerCount, SAVE_PATH);
        }

        MPI.Finalize();
    }

    private static long runOnce(int rank, int size, int workerCount, int rowsPerWorker, int rowsRemainder) {
        MPI.COMM_WORLD.Barrier();
        var start = 0L;

        if (rank == MASTER) {
            start = System.nanoTime();

            var a = Helper.generate(size, MIN, MAX);
            var b = Helper.generate(size, MIN, MAX);
            var flatB = Helper.flatten(b, 0, size, size);

            var startRow = 0;
            for (var worker = 0; worker < workerCount; worker++) {
                var currRows = rowsPerWorker + (worker < rowsRemainder ? 1 : 0);
                var partialFlatA = Helper.flatten(a, startRow, currRows, size);

                MPI.COMM_WORLD.Isend(new int[]{ currRows, size }, 0, 2, MPI.INT, worker, META_TAG);
                MPI.COMM_WORLD.Isend(partialFlatA, 0, currRows * size, MPI.INT, worker, A_TAG);
                MPI.COMM_WORLD.Isend(flatB, 0, size * size, MPI.INT, worker, B_TAG);

                startRow += currRows;
            }
        }

        var localMetaBuff = new int[2];
        MPI.COMM_WORLD.Irecv(localMetaBuff, 0, 2, MPI.INT, MASTER, META_TAG).Wait();

        var localRowCount = localMetaBuff[0];
        var localRowSize = localMetaBuff[1];
        var flatACSize = localRowCount * localRowSize;

        var localFlatA = new int[flatACSize];
        MPI.COMM_WORLD.Irecv(localFlatA, 0, flatACSize, MPI.INT, MASTER, A_TAG).Wait();

        var localFlatB = new int[localRowSize * localRowSize];
        MPI.COMM_WORLD.Irecv(localFlatB, 0, localRowSize * localRowSize, MPI.INT, MASTER, B_TAG).Wait();

        var localA = Helper.unflatten(localFlatA, localRowCount, localRowSize);
        var localB = Helper.unflatten(localFlatB, localRowSize, localRowSize);
        var localC = Helper.multiplyPartial(localA, 0, localRowCount, localB);
        var localFlatC = Helper.flatten(localC, 0, localRowCount, localRowSize);

        MPI.COMM_WORLD.Isend(new int[]{ localRowCount }, 0, 1, MPI.INT, MASTER, META_TAG);
        MPI.COMM_WORLD.Isend(localFlatC, 0, flatACSize, MPI.INT, MASTER, C_TAG);

        if (rank == MASTER) {
            var c = new int[size][size];
            var startRow = 0;
            for (var worker = 0; worker < workerCount; worker++) {
                var metaBuff = new int[1];
                MPI.COMM_WORLD.Irecv(metaBuff, 0, 1, MPI.INT, worker, META_TAG).Wait();

                var rowCount = metaBuff[0];
                var partialFlatC = new int[rowCount * size];
                MPI.COMM_WORLD.Irecv(partialFlatC, 0, partialFlatC.length, MPI.INT, worker, C_TAG).Wait();

                var partialC = Helper.unflatten(partialFlatC, rowCount, size);
                System.arraycopy(partialC, 0, c, startRow, rowCount);
                startRow += rowCount;
            }

            MPI.COMM_WORLD.Barrier();
            return System.nanoTime() - start;
        }

        MPI.COMM_WORLD.Barrier();
        return 0L;
    }
}