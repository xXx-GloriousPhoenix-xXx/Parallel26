package MPIs;

import mpi.MPI;

public class OneToManyMultiplicator extends MatrixMultiplicator {
    public OneToManyMultiplicator(int[][] a, int[][] b) {
        super(a, b);
    }

    @Override
    public int[][] multiply() {
        int rowsPerProcess = aRow / size;
        int remainder = aRow % size;

        int[] sendCounts = new int[size];
        int[] displs = new int[size];
        int offset = 0;
        for (int i = 0; i < size; i++) {
            sendCounts[i] = (rowsPerProcess + (i < remainder ? 1 : 0)) * aCol;
            displs[i] = offset;
            offset += sendCounts[i];
        }

        int localRows = rowsPerProcess + (task < remainder ? 1 : 0);

        int[] flatA = (task == 0) ? MatrixHelper.flatten(a) : null;
        int[] localA = new int[localRows * aCol];
        MPI.COMM_WORLD.Scatterv(flatA, 0, sendCounts, displs, MPI.INT,
                localA, 0, localRows * aCol, MPI.INT, 0);

        int[] flatB = (task == 0) ? MatrixHelper.flatten(b) : new int[aCol * bCol];
        MPI.COMM_WORLD.Bcast(flatB, 0, aCol * bCol, MPI.INT, 0);

        int[][] localResult = MatrixHelper.multiply(
                MatrixHelper.unflatten(localA, localRows, aCol),
                MatrixHelper.unflatten(flatB, aCol, bCol)
        );
        int[] flatLocalResult = MatrixHelper.flatten(localResult);

        int[] recvCounts = new int[size];
        int[] recvDispls = new int[size];
        int recvOffset = 0;
        for (int i = 0; i < size; i++) {
            recvCounts[i] = (rowsPerProcess + (i < remainder ? 1 : 0)) * bCol;
            recvDispls[i] = recvOffset;
            recvOffset += recvCounts[i];
        }

        int[] flatResult = (task == 0) ? new int[aRow * bCol] : null;
        MPI.COMM_WORLD.Gatherv(flatLocalResult, 0, localRows * bCol, MPI.INT,
                flatResult, 0, recvCounts, recvDispls, MPI.INT, 0);

        if (task == 0) {
            return MatrixHelper.unflatten(flatResult, aRow, bCol);
        }
        return null;
    }
}