package lib;

public interface IMultiThreadMatrixMultiplicator {
    int[][] multiply(int[][] a, int[][] b, int threadCount) throws InterruptedException;
}
