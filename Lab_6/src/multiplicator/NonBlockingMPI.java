package multiplicator;

import factory.MatrixGenerator;
import mpi.MPI;
import mpi.Request;

public class NonBlockingMPI {
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
            System.out.println("Running non-blocking");
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

        var r = 0;
        var offset = 0;
        var sendRequests = new Request[3 * workers];
        for (var worker = 1; worker <= workers; worker++) {
            var rows = rowsPerWorker;
            if (worker <= extraRows) {
                rows++;
            }

            sendRequests[r++] = MPI.COMM_WORLD.Isend(new int[] { rows }, 0, 1, MPI.INT, worker, 100);
            sendRequests[r++] = MPI.COMM_WORLD.Isend(A, offset, rows, MPI.OBJECT, worker, 101);
            sendRequests[r++] = MPI.COMM_WORLD.Isend(B, 0, SIZE, MPI.OBJECT, worker, 102);

            offset += rows;
        }
        Request.Waitall(sendRequests);

        offset = 0;
        var receiveRequests = new Request[workers];
        for (var worker = 1; worker <= workers; worker++) {
            var rowsBuffer = new int[1];
            var rR = MPI.COMM_WORLD.Irecv(rowsBuffer, 0, 1, MPI.INT, worker, 103);
            rR.Wait();
            var rows = rowsBuffer[0];

            receiveRequests[worker - 1] = MPI.COMM_WORLD.Irecv(C, offset, rows, MPI.OBJECT, worker, 104);

            offset += rows;
        }
        Request.Waitall(receiveRequests);
    }

    static void runWorker() {
        var rowsBuffer = new int[1];
        var rR = MPI.COMM_WORLD.Irecv(rowsBuffer, 0, 1, MPI.INT, MASTER, 100);
        rR.Wait();
        var rows = rowsBuffer[0];

        var localA = new int[rows][SIZE];
        var B = new int[SIZE][SIZE];
        var rA = MPI.COMM_WORLD.Irecv(localA, 0, rows, MPI.OBJECT, MASTER, 101);
        var rB = MPI.COMM_WORLD.Irecv(B, 0, SIZE, MPI.OBJECT, MASTER, 102);
        Request.Waitall(new Request[] { rA, rB });

//        var localC = new int[rows][SIZE];
//        for (var row = 0; row < rows; row++) {
//            for (var i = 0; i < SIZE; i++) {
//                for (var j = 0; j < SIZE; j++) {
//                    localC[row][i] += localA[row][j] * B[j][i];
//                }
//            }
//        }
        var localC = MatrixHelper.multiply(localA, B);

        var sR = MPI.COMM_WORLD.Isend(new int[] { rows }, 0, 1, MPI.INT, MASTER, 103);
        var sC = MPI.COMM_WORLD.Isend(localC, 0, rows, MPI.OBJECT, MASTER, 104);
        Request.Waitall(new Request[] { sR, sC });
    }
}
