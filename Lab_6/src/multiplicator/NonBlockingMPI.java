package multiplicator;

import factory.MatrixGenerator;
import mpi.MPI;
import mpi.Request;

public class NonBlockingMPI extends MatrixMPI {
    protected static int SIZE = 100;

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
        for (var SIZE : SIZES) {
            var totalTime = 0.0;
            System.out.println(SIZE);

            for (var REPEAT = 0; REPEAT < REPEATS; REPEAT++) {
                var A = MatrixGenerator.generateSquareMatrix(SIZE, MIN, MAX);
                var B = MatrixGenerator.generateSquareMatrix(SIZE, MIN, MAX);
                var C = new int[SIZE][SIZE];

                var workers = size - 1;
                var rowsPerWorker = SIZE / workers;
                var extraRows = SIZE % workers;

                var start = System.nanoTime();

                var offset = 0;
                var sendRequests = new Request[3 * workers];
                var requestIndex = 0;

                for (var worker = 1; worker <= workers; worker++) {
                    var rows = rowsPerWorker;
                    if (worker <= extraRows) {
                        rows++;
                    }

                    sendRequests[requestIndex++] = MPI.COMM_WORLD.Isend(
                            new int[] { rows }, 0, 1, MPI.INT, worker, 100);
                    sendRequests[requestIndex++] = MPI.COMM_WORLD.Isend(
                            A, offset, rows, MPI.OBJECT, worker, 101);
                    sendRequests[requestIndex++] = MPI.COMM_WORLD.Isend(
                            B, 0, SIZE, MPI.OBJECT, worker, 102);

                    offset += rows;
                }

                Request.Waitall(sendRequests);

                offset = 0;
                var receiveRequests = new Request[2 * workers];
                var rowsBuffers = new int[workers][1];

                for (var worker = 1; worker <= workers; worker++) {
                    receiveRequests[2 * (worker - 1)] = MPI.COMM_WORLD.Irecv(
                            rowsBuffers[worker - 1], 0, 1, MPI.INT, worker, 103);
                }

                var rowsWaitRequests = new Request[workers];
                for (var i = 0; i < workers; i++) {
                    rowsWaitRequests[i] = receiveRequests[2 * i];
                }
                Request.Waitall(rowsWaitRequests);

                for (var worker = 1; worker <= workers; worker++) {
                    var rows = rowsBuffers[worker - 1][0];

                    receiveRequests[2 * (worker - 1) + 1] = MPI.COMM_WORLD.Irecv(
                            C, offset, rows, MPI.OBJECT, worker, 104);

                    offset += rows;
                }

                var dataWaitRequests = new Request[workers];
                for (var i = 0; i < workers; i++) {
                    dataWaitRequests[i] = receiveRequests[2 * i + 1];
                }
                Request.Waitall(dataWaitRequests);

                var end = System.nanoTime();
                var time = (end - start) / 1e6;
                totalTime += time;

                System.out.println(" ".repeat(7) + String.format("%-6.3f", time));
            }

            var avgTime = totalTime / REPEATS;
            System.out.println("Avg: " + " ".repeat(2) + String.format("%-6.3f", avgTime));
        }
    }

    static void runWorker() {
        for (var SIZE : SIZES) {
            for (var REPEAT = 0; REPEAT < REPEATS; REPEAT++) {
                var rowsBuffer = new int[1];
                var recvRequests = new Request[3];

                recvRequests[0] = MPI.COMM_WORLD.Irecv(
                        rowsBuffer, 0, 1, MPI.INT, MASTER, 100);

                recvRequests[0].Wait();
                var rows = rowsBuffer[0];

                var localA = new int[rows][SIZE];
                var B = new int[SIZE][SIZE];

                recvRequests[1] = MPI.COMM_WORLD.Irecv(
                        localA, 0, rows, MPI.OBJECT, MASTER, 101);
                recvRequests[2] = MPI.COMM_WORLD.Irecv(
                        B, 0, SIZE, MPI.OBJECT, MASTER, 102);

                Request.Waitall(new Request[] { recvRequests[1], recvRequests[2] });

                var localC = MatrixHelper.multiply(localA, B);

                var sendRequests = new Request[2];
                sendRequests[0] = MPI.COMM_WORLD.Isend(
                        new int[] { rows }, 0, 1, MPI.INT, MASTER, 103);
                sendRequests[1] = MPI.COMM_WORLD.Isend(
                        localC, 0, rows, MPI.OBJECT, MASTER, 104);

                Request.Waitall(sendRequests);
            }
        }
    }
}
