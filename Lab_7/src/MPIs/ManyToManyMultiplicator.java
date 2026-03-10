package MPIs;

import mpi.MPI;

public class ManyToManyMultiplicator extends MatrixMultiplicator {

    public ManyToManyMultiplicator(int[][] a, int[][] b) {
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

        int[] flatResult = new int[aRow * bCol];
        MPI.COMM_WORLD.Allgatherv(flatLocalResult, 0, localRows * bCol, MPI.INT,
                flatResult, 0, recvCounts, recvDispls, MPI.INT);

        return MatrixHelper.unflatten(flatResult, aRow, bCol);
    }
}