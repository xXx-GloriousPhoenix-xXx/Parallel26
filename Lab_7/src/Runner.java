import MPIs.SingleToSingleMPI;
import mpi.MPI;
import mpi.MPIException;

private static final int MASTER = 0;
private static final int[] SIZES = IntStream
        .iterate(500, i -> i + 500)
        .limit(6)
        .toArray();
private static final int MIN = 1, MAX = 100;
private static final int REPEATS = 10;

public static void main(String[] args) throws MPIException {
    MPI.Init(args);

    var task = MPI.COMM_WORLD.Rank();
    var size = MPI.COMM_WORLD.Size();

    if (size < 2) {
        if (task == MASTER) {
            System.err.println("Program requires at least 2 processes");
        }
        MPI.COMM_WORLD.Abort(1);
    }

    if (task == MASTER) {
        System.out.printf("Total threads: %d%n", size);
        System.out.printf("%-10s | %-15s | %-15s | %-15s | %-15s%n",
                "Size", "1-1 (ms)", "1-N (ms)", "N-1 (ms)", "N-N (ms)");
    }

    for (var SIZE : SIZES) {
        var a = MatrixHelper.generate(SIZE, MIN, MAX);
        var b = MatrixHelper.generate(SIZE, MIN, MAX);

        double ooTime = 0.0, omTime = 0.0, moTime = 0.0, mmTime = 0.0;

        var oo = new SingleToSingleMPI(a, b);
        // init om
        // init mo
        // init mm

        long start, end;
        for (var REPEAT = 0; REPEAT < REPEATS; REPEAT++) {
            // One to One
            if (task == MASTER) {
                start = System.nanoTime();
                oo.multiply();
                ooTime += (System.nanoTime() - start) / 1e6;
            }

            MPI.COMM_WORLD.Barrier();

            // One to Many
            start = System.nanoTime();
            //
//            System.err.println("One to Many not implemented");
            //
            end = System.nanoTime();
            if (task == MASTER) {
                omTime += (end - start) / 1e6;
            }

            MPI.COMM_WORLD.Barrier();

            // Many to One
            start = System.nanoTime();
            //
//            System.err.println("Many to One not implemented");
            //
            end = System.nanoTime();
            if (task == MASTER) {
                moTime += (end - start) / 1e6;
            }

            MPI.COMM_WORLD.Barrier();

            // Many to Many
            start = System.nanoTime();
            //
//            System.err.println("Many to Many not implemented");
            //
            end = System.nanoTime();
            if (task == MASTER) {
                mmTime += (end - start) / 1e6;
            }

            MPI.COMM_WORLD.Barrier();
        }

        if (task == MASTER) {
            double avgOOTime = ooTime / REPEATS,
                   avgOMTime = omTime / REPEATS,
                   avgMOTime = moTime / REPEATS,
                   avgMMTime = mmTime / REPEATS;

            System.out.printf("%-10d | %-15.3f | %-15.3f | %-15.3f | %-15.3f%n",
                    SIZE, avgOOTime, avgOMTime, avgMOTime, avgMMTime
            );
        }

        MPI.COMM_WORLD.Barrier();
    }

    MPI.Finalize();
}
