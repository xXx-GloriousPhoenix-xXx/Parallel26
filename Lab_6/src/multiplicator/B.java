package multiplicator;

import mpi.*;
import utils.MatrixOperator;

public class B {
    private static final int MASTER = 0;
    private static final int META_TAG = 0;
    private static final int A_TAG = 1;
    private static final int B_TAG = 2;
    private static final int C_TAG = 3;

    public static void main(String[] args) {
        MPI.Init(args);
        var rank = MPI.COMM_WORLD.Rank();
        var workerCount = MPI.COMM_WORLD.Size();

        if (rank == MASTER) {
            final var SIZE = 16;
            final var MIN = 1;
            final var MAX = 100;

            var a = MatrixOperator.generate(SIZE, MIN, MAX);
            var b = MatrixOperator.generate(SIZE, MIN, MAX);
            var c = new int[SIZE][SIZE];

            var rowsPerWorker = SIZE / workerCount;
            var rowsRemainder = SIZE % workerCount;

            var flatB = MatrixOperator.flatten(b, 0, SIZE, SIZE);

            // send section
            var startRow = 0;
            for (var workerIndex = 0; workerIndex < workerCount - 1; workerIndex++) {
                var currRows = rowsPerWorker + (workerIndex < rowsRemainder ? 1 : 0);
                var workerAddressIndex = workerIndex + 1;

                // send rowCount of partial A and rowSize
                MPI.COMM_WORLD.Send(new int[] { currRows, SIZE }, 0, 2, MPI.INT, workerAddressIndex, META_TAG);

                // send flat partial A
                var flatPartialA = MatrixOperator.flatten(a, startRow, currRows, SIZE);
                MPI.COMM_WORLD.Send(flatPartialA, 0, currRows * SIZE, MPI.INT, workerAddressIndex, A_TAG);

                // send flat B
                MPI.COMM_WORLD.Send(flatB, 0, SIZE * SIZE, MPI.INT, workerAddressIndex, B_TAG);

                startRow += currRows;
            }

            // own calculation section
            var lastRow = startRow + rowsPerWorker;
            var lastPartialC = MatrixOperator.multiplyPartial(a, startRow, lastRow, b);

            // receive section
            startRow = 0;
            for (var workerIndex = 0; workerIndex < workerCount - 1; workerIndex++) {
                var workerAddressIndex = workerIndex + 1;

                // receive rowCount
                var rowCountBuff = new int[1];
                MPI.COMM_WORLD.Recv(rowCountBuff, 0, 1, MPI.INT, workerAddressIndex, META_TAG);
                var rowCount = rowCountBuff[0];

                // receive flat partial C
                var flatPartialCSize = rowCount * SIZE;
                var flatPartialC = new int[flatPartialCSize];
                MPI.COMM_WORLD.Recv(flatPartialC, 0, flatPartialCSize, MPI.INT, workerAddressIndex, C_TAG);
                var partialC = MatrixOperator.unflatten(flatPartialC, rowCount, SIZE);

                for (var row = 0; row < rowCount; row++) {
                    c[startRow + row] = partialC[row];
                }

                startRow += rowCount;
            }

            // add final
            for (var row = 0; row < rowsPerWorker; row++) {
                c[startRow + row] = lastPartialC[row];
            }

            MatrixOperator.display(a, true);
            MatrixOperator.display(b, true);
            MatrixOperator.display(c, true);
            MatrixOperator.display(MatrixOperator.multiplyComplete(a, b), false);
        }
        else {
            // receive rowCount of partial A and rowSize
            var metaBuff = new int[2];
            MPI.COMM_WORLD.Recv(metaBuff, 0, 2, MPI.INT, MASTER, META_TAG);
            var rowCount = metaBuff[0];
            var rowSize = metaBuff[1];

            // receive flat partial A
            var flatPartialASize = rowCount * rowSize;
            var flatPartialA = new int[flatPartialASize];
            MPI.COMM_WORLD.Recv(flatPartialA, 0, flatPartialASize, MPI.INT, MASTER, A_TAG);
            var partialA = MatrixOperator.unflatten(flatPartialA, rowCount, rowSize);

            // receive flat B
            var flatBSize = rowSize * rowSize;
            var flatB = new int[flatBSize];
            MPI.COMM_WORLD.Recv(flatB, 0, flatBSize, MPI.INT, MASTER, B_TAG);
            var b = MatrixOperator.unflatten(flatB, rowSize, rowSize);

            // calculate partial C
            var partialC = MatrixOperator.multiplyPartial(partialA, 0, rowCount, b);
            var flatPartialC = MatrixOperator.flatten(partialC, 0, rowCount, rowSize);

            // send rowCount
            MPI.COMM_WORLD.Send(new int[] { rowCount }, 0, 1, MPI.INT, MASTER, META_TAG);

            // send flat partial C
            MPI.COMM_WORLD.Send(flatPartialC, 0, rowCount * rowSize, MPI.INT, MASTER, C_TAG);
        }

        MPI.Finalize();
    }
}
