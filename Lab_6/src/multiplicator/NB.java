package multiplicator;

import mpi.*;
import utils.MatrixOperator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class NB {
    private static final int MASTER = 0;
    private static final int META_TAG = 0;
    private static final int A_TAG = 1;
    private static final int B_TAG = 2;
    private static final int C_TAG = 3;

    private static final int SIZE_FROM = 500;
    private static final int SIZE_TO = 4000;
    private static final int SIZE_STEP = 500;
    private static final int REPEATS = 5;
    private static final String CSV_PATH = "F:\\Programmes\\Github\\Reps\\Parallel26\\Lab_6\\src\\NBResult.csv";

    public static void main(String[] args) {
        MPI.Init(args);
        var rank = MPI.COMM_WORLD.Rank();
        var workerCount = MPI.COMM_WORLD.Size();

        if (rank == MASTER) {
            try (var writer = new PrintWriter(new FileWriter(CSV_PATH))) {
                writer.println("MatrixSize;Run1_ms;Run2_ms;Run3_ms;Run4_ms;Run5_ms;Average_ms");

                for (var SIZE = SIZE_FROM; SIZE <= SIZE_TO; SIZE += SIZE_STEP) {
                    System.out.printf("Benchmarking SIZE=%d...%n", SIZE);
                    var times = new long[REPEATS];

                    for (var repeat = 0; repeat < REPEATS; repeat++) {
                        var a = MatrixOperator.generate(SIZE, 1, 100);
                        var b = MatrixOperator.generate(SIZE, 1, 100);
                        var c = new int[SIZE][SIZE];

                        var rowsPerWorker = SIZE / workerCount;
                        var rowsRemainder = SIZE % workerCount;
                        var flatB = MatrixOperator.flatten(b, 0, SIZE, SIZE);

                        for (var wi = 1; wi < workerCount; wi++) {
                            MPI.COMM_WORLD.Send(new int[]{1, SIZE}, 0, 2, MPI.INT, wi, META_TAG);
                        }

                        var startTime = System.currentTimeMillis();

                        var metaBuffers = new int[workerCount - 1][];
                        var partialABuffers = new int[workerCount - 1][];
                        var sendRequests = new Request[(workerCount - 1) * 3];

                        var startRow = 0;
                        for (var wi = 0; wi < workerCount - 1; wi++) {
                            var currRows = rowsPerWorker + (wi < rowsRemainder ? 1 : 0);
                            var dest = wi + 1;
                            var base = wi * 3;

                            metaBuffers[wi] = new int[]{currRows, SIZE};
                            partialABuffers[wi] = MatrixOperator.flatten(a, startRow, currRows, SIZE);

                            sendRequests[base] = MPI.COMM_WORLD.Isend(metaBuffers[wi], 0, 2, MPI.INT, dest, META_TAG + 10);
                            sendRequests[base + 1] = MPI.COMM_WORLD.Isend(partialABuffers[wi], 0, currRows * SIZE, MPI.INT, dest, A_TAG);
                            sendRequests[base + 2] = MPI.COMM_WORLD.Isend(flatB, 0, SIZE * SIZE, MPI.INT, dest, B_TAG);

                            startRow += currRows;
                        }

                        var masterStartRow = startRow;
                        var masterEndRow = startRow + rowsPerWorker + (rowsRemainder > 0 && (workerCount - 1) < rowsRemainder ? 1 : 0);
                        var lastPartialC = MatrixOperator.multiplyPartial(a, masterStartRow, masterEndRow, b);
                        var masterRows = masterEndRow - masterStartRow;

                        var rowCountBuffers  = new int[workerCount - 1][1];
                        var flatCBuffers = new int[workerCount - 1][];
                        var metaRecvReqs = new Request[workerCount - 1];
                        var dataRecvReqs = new Request[workerCount - 1];

                        for (var wi = 0; wi < workerCount - 1; wi++) {
                            metaRecvReqs[wi] = MPI.COMM_WORLD.Irecv(
                                    rowCountBuffers[wi], 0, 1, MPI.INT, wi + 1, META_TAG
                            );
                        }

                        Request.Waitall(sendRequests);

                        Request.Waitall(metaRecvReqs);

                        for (var wi = 0; wi < workerCount - 1; wi++) {
                            var rowCount = rowCountBuffers[wi][0];
                            flatCBuffers[wi] = new int[rowCount * SIZE];
                            dataRecvReqs[wi] = MPI.COMM_WORLD.Irecv(
                                    flatCBuffers[wi], 0, rowCount * SIZE, MPI.INT, wi + 1, C_TAG
                            );
                        }

                        startRow = 0;
                        for (var wi = 0; wi < workerCount - 1; wi++) {
                            startRow += rowCountBuffers[wi][0];
                        }
                        for (var row = 0; row < masterRows; row++) {
                            c[startRow + row] = lastPartialC[row];
                        }

                        var remaining = workerCount - 1;
                        var done = new boolean[workerCount - 1];
                        startRow = 0;
                        var cumulativeStarts = new int[workerCount - 1];
                        var acc = 0;
                        for (var wi = 0; wi < workerCount - 1; wi++) {
                            cumulativeStarts[wi] = acc;
                            acc += rowCountBuffers[wi][0];
                        }

                        while (remaining > 0) {
                            var status = Request.Waitany(dataRecvReqs);
                            var wi = status.index;
                            if (!done[wi]) {
                                done[wi] = true;
                                remaining--;
                                var rowCount = rowCountBuffers[wi][0];
                                var partialC = MatrixOperator.unflatten(flatCBuffers[wi], rowCount, SIZE);
                                for (var row = 0; row < rowCount; row++) {
                                    c[cumulativeStarts[wi] + row] = partialC[row];
                                }
                            }
                        }

                        var elapsed = System.currentTimeMillis() - startTime;
                        times[repeat] = elapsed;
                        System.out.printf("  Run %d: %d ms%n", repeat + 1, elapsed);
                    }

                    for (var wi = 1; wi < workerCount; wi++) {
                        MPI.COMM_WORLD.Send(new int[]{0, 0}, 0, 2, MPI.INT, wi, META_TAG);
                    }

                    var sum = 0L;
                    var sb = new StringBuilder();
                    sb.append(SIZE);
                    for (var t : times) {
                        sb.append(";").append(t);
                        sum += t;
                    }
                    var avg = (double) sum / REPEATS;
                    sb.append(String.format(";%.2f", avg));
                    writer.println(sb);
                    System.out.printf("  Average: %.2f ms%n", avg);
                }

                for (var wi = 1; wi < workerCount; wi++) {
                    MPI.COMM_WORLD.Send(new int[]{-1, 0}, 0, 2, MPI.INT, wi, META_TAG);
                }

            } catch (IOException e) {
                System.err.println("Failed to write CSV: " + e.getMessage());
            }

        } else {
            while (true) {
                var controlBuff = new int[2];
                MPI.COMM_WORLD.Recv(controlBuff, 0, 2, MPI.INT, MASTER, META_TAG);
                if (controlBuff[0] == -1) break;
                if (controlBuff[0] ==  0) continue;

                var metaBuff = new int[2];
                var metaReq = MPI.COMM_WORLD.Irecv(metaBuff, 0, 2, MPI.INT, MASTER, META_TAG + 10);
                metaReq.Wait();

                var rowCount = metaBuff[0];
                var rowSize = metaBuff[1];
                var flatPartialA = new int[rowCount * rowSize];
                var flatB = new int[rowSize * rowSize];

                var recvA = MPI.COMM_WORLD.Irecv(flatPartialA, 0, rowCount * rowSize, MPI.INT, MASTER, A_TAG);
                var recvB = MPI.COMM_WORLD.Irecv(flatB, 0, rowSize  * rowSize, MPI.INT, MASTER, B_TAG);
                Request.Waitall(new Request[]{recvA, recvB});

                var partialA = MatrixOperator.unflatten(flatPartialA, rowCount, rowSize);
                var b = MatrixOperator.unflatten(flatB, rowSize, rowSize);
                var partialC = MatrixOperator.multiplyPartial(partialA, 0, rowCount, b);
                var flatPartialC = MatrixOperator.flatten(partialC, 0, rowCount, rowSize);

                var sendMeta = MPI.COMM_WORLD.Isend(new int[]{rowCount}, 0, 1,            MPI.INT, MASTER, META_TAG);
                var sendC = MPI.COMM_WORLD.Isend(flatPartialC, 0, rowCount * rowSize, MPI.INT, MASTER, C_TAG);
                Request.Waitall(new Request[]{sendMeta, sendC});
            }
        }

        MPI.Finalize();
    }
}