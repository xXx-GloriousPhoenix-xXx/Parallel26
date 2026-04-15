package multiplicator;

import mpi.MPI;

public class Collective {
    private static final int MASTER = 0;
    private static final int SIZE = 16;
    private static final int MIN = 1;
    private static final int MAX = 100;

    public static void main(String[] args) {
        MPI.Init(args);

        var rank = MPI.COMM_WORLD.Rank();
        var workerCount = MPI.COMM_WORLD.Size();
        var rowsPerWorker = SIZE / workerCount;

        var flatA = new int[SIZE * SIZE];
        var flatB = new int[SIZE * SIZE];
        var localFlatA = new int[rowsPerWorker * SIZE];
        var localFlatC = new int[rowsPerWorker * SIZE];

        if (rank == MASTER) {
            var a = Helper.generate(SIZE, MIN, MAX);
            var b = Helper.generate(SIZE, MIN, MAX);
            flatA = Helper.flatten(a, 0, SIZE, SIZE);
            flatB = Helper.flatten(b, 0, SIZE, SIZE);
        }

        MPI.COMM_WORLD.Bcast(flatB, 0, SIZE * SIZE, MPI.INT, MASTER);

        MPI.COMM_WORLD.Scatter(
                flatA, 0, rowsPerWorker * SIZE, MPI.INT,
                localFlatA, 0, rowsPerWorker * SIZE, MPI.INT,
                MASTER
        );

        var localA = Helper.unflatten(localFlatA, rowsPerWorker, SIZE);
        var localB = Helper.unflatten(flatB, SIZE, SIZE);
        var localC = Helper.multiplyPartial(localA, 0, rowsPerWorker, localB);
        localFlatC = Helper.flatten(localC, 0, rowsPerWorker, SIZE);

        var flatC = new int[SIZE * SIZE];
        MPI.COMM_WORLD.Gather(
                localFlatC, 0, rowsPerWorker * SIZE, MPI.INT,
                flatC, 0, rowsPerWorker * SIZE, MPI.INT,
                MASTER
        );

        if (rank == MASTER) {
            var c = Helper.unflatten(flatC, SIZE, SIZE);
            Helper.display(c, false);
        }

        MPI.Finalize();
    }
}