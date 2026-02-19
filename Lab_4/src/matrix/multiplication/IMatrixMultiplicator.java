package matrix.multiplication;

public interface IMatrixMultiplicator {
    int[][] multiplyClassic(int[][] a, int[][] b, int threadCount);
    int[][] multiplyForkJoin(int[][] a, int[][] b, int threadCount);
}
