package multiplicator;

import mpi.*;

public class Simple {
    private static final int MASTER = 0;
    private static final int META_TAG = 0;
    private static final int A_TAG = 1;
    private static final int B_TAG = 2;
    private static final int C_TAG = 3;

    private static final int SIZE = 16;
    private static final int MIN = 1;
    private static final int MAX = 100;

    public static void main(String[] args) {
        MPI.Init(args);

        var rank = MPI.COMM_WORLD.Rank();
        var workerCount = MPI.COMM_WORLD.Size();

        if (rank == MASTER) {
            var a = Helper.generate(SIZE, MIN, MAX);
            var b = Helper.generate(SIZE, MIN, MAX);

            var rowsPerWorker = SIZE / workerCount;
            var rowsRemainder = SIZE % workerCount;

            var flatB = Helper.flatten(b, 0, SIZE, SIZE);

            var startRow = 0;
            for (var worker = 0; worker < workerCount; worker++) {
                var currRows = rowsPerWorker + (worker < rowsRemainder ? 1 : 0);
                var partialFlatA = Helper.flatten(a, startRow, currRows, SIZE);

                MPI.COMM_WORLD.Isend(new int[] { currRows, SIZE }, 0, 2, MPI.INT, worker, META_TAG);
                MPI.COMM_WORLD.Isend(partialFlatA, 0, currRows * SIZE, MPI.INT, worker, A_TAG);
                MPI.COMM_WORLD.Isend(flatB, 0, SIZE * SIZE, MPI.INT, worker, B_TAG);

                startRow += currRows;
            }
        }

        var localMetaBuff = new int[2];
        var localMetaReq = MPI.COMM_WORLD.Irecv(localMetaBuff, 0, 2, MPI.INT, MASTER, META_TAG);
        localMetaReq.Wait();

        var localRowCount = localMetaBuff[0];
        var localRowSize = localMetaBuff[1];

        var flatACSize = localRowCount * localRowSize;
        var localFlatA = new int[flatACSize];

        var aReq = MPI.COMM_WORLD.Irecv(localFlatA, 0, flatACSize, MPI.INT, MASTER, A_TAG);
        aReq.Wait();

        var localA = Helper.unflatten(localFlatA, localRowCount, localRowSize);

        var flatBSize = localRowSize * localRowSize;
        var localFlatB = new int[flatBSize];

        var bReq = MPI.COMM_WORLD.Irecv(localFlatB, 0, flatBSize, MPI.INT, MASTER, B_TAG);
        bReq.Wait();

        var localB = Helper.unflatten(localFlatB, localRowSize, localRowSize);

        var localC = Helper.multiplyPartial(localA, 0, localRowCount, localB);
        var localFlatC = Helper.flatten(localC, 0, localRowCount, localRowSize);

        MPI.COMM_WORLD.Isend(new int[] { localRowCount }, 0, 1, MPI.INT, MASTER, META_TAG);
        MPI.COMM_WORLD.Isend(localFlatC, 0, flatACSize, MPI.INT, MASTER, C_TAG);

        if (rank == MASTER) {
            var c = new int[SIZE][SIZE];

            var startRow = 0;
            for (var worker = 0; worker < workerCount; worker++) {
                var metaBuff = new int[1];
                var metaReq = MPI.COMM_WORLD.Irecv(metaBuff, 0, 1, MPI.INT, worker, META_TAG);
                metaReq.Wait();

                var rowCount = metaBuff[0];

                var partialFlatCSize = rowCount * SIZE;
                var partialFlatC = new int[partialFlatCSize];

                var cReq = MPI.COMM_WORLD.Irecv(partialFlatC, 0, partialFlatC.length, MPI.INT, worker, C_TAG);
                cReq.Wait();

                var partialC = Helper.unflatten(partialFlatC, rowCount, SIZE);

                if (rowCount >= 0) {
                    System.arraycopy(partialC, 0, c, startRow, rowCount);
                }

                startRow += rowCount;
            }

            Helper.display(c, false);
        }

        MPI.Finalize();
    }
}
