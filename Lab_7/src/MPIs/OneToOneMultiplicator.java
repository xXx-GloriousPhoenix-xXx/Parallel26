package MPIs;

import mpi.MPI;

public class OneToOneMultiplicator extends MatrixMultiplicator {

    public OneToOneMultiplicator(int[][] a, int[][] b) {
        super(a, b);
    }

    @Override
    public int[][] multiply() {
        var rowsPerProcess = aRow / size;
        var remainder = aRow % size;

        var flatB = MatrixHelper.flatten(b);

        if (task == 0) {
            var offset = 0;
            for (var dest = 1; dest < size; dest++) {
                var rows = rowsPerProcess + (dest < remainder ? 1 : 0);
                var chunk = new int[rows * aCol];
                System.arraycopy(MatrixHelper.flatten(a), offset, chunk, 0, rows * aCol);

                MPI.COMM_WORLD.Send(new int[]{rows}, 0, 1, MPI.INT, dest, 0);
                MPI.COMM_WORLD.Send(chunk, 0, rows * aCol, MPI.INT, dest, 1);
                MPI.COMM_WORLD.Send(flatB, 0, aCol * bCol, MPI.INT, dest, 2);

                offset += rows * aCol;
            }

            var masterRows = rowsPerProcess + (0 < remainder ? 1 : 0);
            var localA = new int[masterRows][];
            System.arraycopy(a, 0, localA, 0, masterRows);
            var partialResult = MatrixHelper.multiply(localA, b);

            var result = new int[aRow][bCol];
            System.arraycopy(partialResult, 0, result, 0, masterRows);

            var rowOffset = masterRows;
            for (var src = 1; src < size; src++) {
                var rows = rowsPerProcess + (src < remainder ? 1 : 0);
                var recvResult = new int[rows * bCol];
                MPI.COMM_WORLD.Recv(recvResult, 0, rows * bCol, MPI.INT, src, 3);

                for (var i = 0; i < rows; i++)
                    System.arraycopy(recvResult, i * bCol, result[rowOffset + i], 0, bCol);
                rowOffset += rows;
            }

            return result;

        } else {
            var rowsBuf = new int[1];
            MPI.COMM_WORLD.Recv(rowsBuf, 0, 1, MPI.INT, 0, 0);
            var localRows = rowsBuf[0];

            var localFlatA = new int[localRows * aCol];
            MPI.COMM_WORLD.Recv(localFlatA, 0, localRows * aCol, MPI.INT, 0, 1);

            var localFlatB = new int[aCol * bCol];
            MPI.COMM_WORLD.Recv(localFlatB, 0, aCol * bCol, MPI.INT, 0, 2);

            var localA = MatrixHelper.unflatten(localFlatA, localRows, aCol);
            var localB = MatrixHelper.unflatten(localFlatB, aCol, bCol);

            var localResult = MatrixHelper.multiply(localA, localB);
            var flatResult = MatrixHelper.flatten(localResult);

            MPI.COMM_WORLD.Send(flatResult, 0, localRows * bCol, MPI.INT, 0, 3);

            return null;
        }
    }
}