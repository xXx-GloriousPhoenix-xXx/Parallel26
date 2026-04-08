package matrix.multiplication;

public interface IMatrixMultiplicator {
    int[][] multiplySequential(int[][] a, int[][] b);
    int[][] multiplyForkJoin(int[][] a, int[][] b);
}
