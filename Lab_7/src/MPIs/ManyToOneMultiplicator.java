package MPIs;

import mpi.MPI;

public class ManyToOneMultiplicator extends MatrixMultiplicator {
    public ManyToOneMultiplicator(int[][] a, int[][] b) {
        super(a, b);
    }

    @Override
    public int[][] multiply() {
        int rowsPerProcess = aRow / size;
        int remainder = aRow % size;

        int localRows = rowsPerProcess + (task < remainder ? 1 : 0);
        int startRow = task * rowsPerProcess + Math.min(task, remainder);

        int[][] localA = new int[localRows][];
        for (int i = 0; i < localRows; i++)
            localA[i] = a[startRow + i];

        int[] flatLocalResult = MatrixHelper.flatten(MatrixHelper.multiply(localA, b));

        int[] recvCounts = new int[size];
        int[] recvDispls = new int[size];
        int offset = 0;
        for (int i = 0; i < size; i++) {
            recvCounts[i] = (rowsPerProcess + (i < remainder ? 1 : 0)) * bCol;
            recvDispls[i] = offset;
            offset += recvCounts[i];
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