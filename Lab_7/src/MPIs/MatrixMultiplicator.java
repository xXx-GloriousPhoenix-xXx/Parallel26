package MPIs;

import mpi.MPI;

public abstract class MatrixMultiplicator {
    protected int task, size;
    protected int[][] a, b;
    protected int aRow, aCol, bCol;

    public MatrixMultiplicator(int[][] a, int[][] b) {
        this.a = a;
        this.b = b;

        aRow = a.length;
        aCol = a[0].length;
        bCol = b[0].length;

        task = MPI.COMM_WORLD.Rank();
        size = MPI.COMM_WORLD.Size();
    }

    public abstract int[][] multiply();
}
