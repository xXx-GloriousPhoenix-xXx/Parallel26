package MPIs;

import mpi.MPI;

public abstract class MatrixMPI {
    protected int task, size;
    protected int aRow, aCol, bCol;
    protected int[][] a, b;

    public MatrixMPI(int[][] a, int[][] b) {
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
