package multiplicator;

import factory.*;
import mpi.*;

public class BlockingMPI {
    private static final int MASTER = 0;
    private static final int SIZE = 3, MAX = 100, MIN = 0;

    public static void main(String[] args) {
        MPI.Init(args);

        var task = MPI.COMM_WORLD.Rank();
        var size = MPI.COMM_WORLD.Size();

        if (task == MASTER) {
            if (size < 2) {
                MPI.COMM_WORLD.Abort(1);
            }
        }

        if (task == MASTER) {
            System.out.println("Running blocking");
            runMaster(size);
        }
        else {
            runWorker();
        }

        MPI.Finalize();
    }

    static void runMaster(int size) {
        var A = MatrixGenerator.generateSquareMatrix(SIZE, MIN, MAX);
        var B = MatrixGenerator.generateSquareMatrix(SIZE, MIN, MAX);
        var C = new int[SIZE][SIZE];

        var workers = size - 1;
        var rowsPerWorker = SIZE / workers;
        var extraRows = SIZE % workers;

        var offset = 0;
        for (var worker = 1; worker <= workers; worker++) {
            var rows = rowsPerWorker;
            if (workers <= extraRows) {
                rows++;
            }

            MPI.COMM_WORLD.Send(new int[] { rows }, 0, 1, MPI.INT, worker, 100);
            MPI.COMM_WORLD.Send(A, offset, rows, MPI.OBJECT, worker, 101);
            MPI.COMM_WORLD.Send(B, 0, SIZE, MPI.OBJECT, worker, 102);
            offset += rows;
        }

        offset = 0;
        for (var worker = 1; worker <= workers; worker++) {
            var rowsBuffer = new int[1];
            MPI.COMM_WORLD.Recv(rowsBuffer, 0, 1, MPI.INT, worker, 103);
            var rows = rowsBuffer[0];

            MPI.COMM_WORLD.Recv(C, offset, rows, MPI.OBJECT, worker, 104);
            offset += rows;
        }

        print(A);
        print(B);
        print(C);
    }

    static void runWorker() {
        var rowsBuffer = new int[1];
        MPI.COMM_WORLD.Recv(rowsBuffer, 0, 1, MPI.INT, MASTER, 100);
        var rows = rowsBuffer[0];

        var localA = new int[rows][SIZE];
        var B = new int[SIZE][SIZE];
        MPI.COMM_WORLD.Recv(localA, 0, rows, MPI.OBJECT, MASTER, 101);
        MPI.COMM_WORLD.Recv(B, 0, SIZE, MPI.OBJECT, MASTER, 102);

        var localC = new int[rows][SIZE];
        for (var row = 0; row < rows; row++) {
            for (var i = 0; i < SIZE; i++) {
                for (var j = 0; j < SIZE; j++) {
                    localC[row][i] += localA[row][j] * B[j][i];
                }
            }
        }

        MPI.COMM_WORLD.Send(new int[] { rows }, 0, 1, MPI.INT, MASTER, 103);
        MPI.COMM_WORLD.Send(localC, 0, rows, MPI.OBJECT, MASTER, 104);
    }

    static void print(int[][] matrix) {
        for (var row : matrix) {
            for (var col : row) {
                System.out.format("%d ", col);
            }
            System.out.println();
        }
    }
}