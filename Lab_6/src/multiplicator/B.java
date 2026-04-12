package multiplicator;

import mpi.*;
import utils.MatrixOperator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class B {
    private static final int MASTER = 0;
    private static final int META_TAG = 0;
    private static final int A_TAG = 1;
    private static final int B_TAG = 2;
    private static final int C_TAG = 3;

    private static final int SIZE_FROM = 500;
    private static final int SIZE_TO = 4000;
    private static final int SIZE_STEP = 500;
    private static final int REPEATS = 5;
    private static final String CSV_PATH = "F:\\Programmes\\Github\\Reps\\Parallel26\\Lab_6\\src\\BResult.csv";

    public static void main(String[] args) {
        MPI.Init(args);
        var rank = MPI.COMM_WORLD.Rank();
        var workerCount = MPI.COMM_WORLD.Size();

        if (rank == MASTER) {
            try (var writer = new PrintWriter(new FileWriter(CSV_PATH))) {
                writer.println("MatrixSize;Run1_ms;Run2_ms;Run3_ms;Run4_ms;Run5_ms;Average_ms");

                for (int SIZE = SIZE_FROM; SIZE <= SIZE_TO; SIZE += SIZE_STEP) {
                    System.out.printf("Benchmarking SIZE=%d...%n", SIZE);
                    long[] times = new long[REPEATS];

                    for (int repeat = 0; repeat < REPEATS; repeat++) {
                        final int MIN = 1;
                        final int MAX = 100;

                        var a = MatrixOperator.generate(SIZE, MIN, MAX);
                        var b = MatrixOperator.generate(SIZE, MIN, MAX);
                        var c = new int[SIZE][SIZE];

                        var rowsPerWorker = SIZE / workerCount;
                        var rowsRemainder = SIZE % workerCount;

                        var flatB = MatrixOperator.flatten(b, 0, SIZE, SIZE);

                        for (var workerIndex = 1; workerIndex < workerCount; workerIndex++) {
                            MPI.COMM_WORLD.Send(new int[]{1, SIZE}, 0, 2, MPI.INT, workerIndex, META_TAG);
                        }

                        long startTime = System.currentTimeMillis();

                        var startRow = 0;
                        for (var workerIndex = 0; workerIndex < workerCount - 1; workerIndex++) {
                            var currRows = rowsPerWorker + (workerIndex < rowsRemainder ? 1 : 0);
                            var workerAddressIndex = workerIndex + 1;

                            MPI.COMM_WORLD.Send(new int[]{currRows, SIZE}, 0, 2, MPI.INT, workerAddressIndex, META_TAG + 10);

                            var flatPartialA = MatrixOperator.flatten(a, startRow, currRows, SIZE);
                            MPI.COMM_WORLD.Send(flatPartialA, 0, currRows * SIZE, MPI.INT, workerAddressIndex, A_TAG);

                            MPI.COMM_WORLD.Send(flatB, 0, SIZE * SIZE, MPI.INT, workerAddressIndex, B_TAG);

                            startRow += currRows;
                        }

                        var lastRow = startRow + rowsPerWorker;
                        var lastPartialC = MatrixOperator.multiplyPartial(a, startRow, lastRow, b);

                        startRow = 0;
                        for (var workerIndex = 0; workerIndex < workerCount - 1; workerIndex++) {
                            var workerAddressIndex = workerIndex + 1;

                            var rowCountBuff = new int[1];
                            MPI.COMM_WORLD.Recv(rowCountBuff, 0, 1, MPI.INT, workerAddressIndex, META_TAG);
                            var rowCount = rowCountBuff[0];

                            var flatPartialCSize = rowCount * SIZE;
                            var flatPartialC = new int[flatPartialCSize];
                            MPI.COMM_WORLD.Recv(flatPartialC, 0, flatPartialCSize, MPI.INT, workerAddressIndex, C_TAG);
                            var partialC = MatrixOperator.unflatten(flatPartialC, rowCount, SIZE);

                            for (var row = 0; row < rowCount; row++) {
                                c[startRow + row] = partialC[row];
                            }
                            startRow += rowCount;
                        }

                        for (var row = 0; row < rowsPerWorker; row++) {
                            c[startRow + row] = lastPartialC[row];
                        }

                        long elapsed = System.currentTimeMillis() - startTime;
                        times[repeat] = elapsed;
                        System.out.printf("  Run %d: %d ms%n", repeat + 1, elapsed);
                    }

                    for (var workerIndex = 1; workerIndex < workerCount; workerIndex++) {
                        MPI.COMM_WORLD.Send(new int[]{0, 0}, 0, 2, MPI.INT, workerIndex, META_TAG);
                    }

                    long sum = 0;
                    var sb = new StringBuilder();
                    sb.append(SIZE);
                    for (long t : times) {
                        sb.append(";").append(t);
                        sum += t;
                    }
                    double avg = (double) sum / REPEATS;
                    sb.append(String.format(";%.2f", avg));
                    writer.println(sb);

                    System.out.printf("  Average: %.2f ms%n", avg);
                }

                for (var workerIndex = 1; workerIndex < workerCount; workerIndex++) {
                    MPI.COMM_WORLD.Send(new int[]{-1, 0}, 0, 2, MPI.INT, workerIndex, META_TAG);
                }
            } catch (IOException e) {
                System.err.println("Failed to write CSV: " + e.getMessage());
            }

        } else {
            // Worker: outer loop — listen for SIZE signals
            while (true) {
                var controlBuff = new int[2];
                MPI.COMM_WORLD.Recv(controlBuff, 0, 2, MPI.INT, MASTER, META_TAG);
                int control = controlBuff[0];
                int SIZE = controlBuff[1];

                if (control == -1) break;
                if (control == 0) continue;

                var metaBuff = new int[2];
                MPI.COMM_WORLD.Recv(metaBuff, 0, 2, MPI.INT, MASTER, META_TAG + 10);
                var rowCount = metaBuff[0];
                var rowSize = metaBuff[1];

                var flatPartialASize = rowCount * rowSize;
                var flatPartialA = new int[flatPartialASize];
                MPI.COMM_WORLD.Recv(flatPartialA, 0, flatPartialASize, MPI.INT, MASTER, A_TAG);
                var partialA = MatrixOperator.unflatten(flatPartialA, rowCount, rowSize);

                var flatBSize = rowSize * rowSize;
                var flatB = new int[flatBSize];
                MPI.COMM_WORLD.Recv(flatB, 0, flatBSize, MPI.INT, MASTER, B_TAG);
                var b = MatrixOperator.unflatten(flatB, rowSize, rowSize);

                var partialC = MatrixOperator.multiplyPartial(partialA, 0, rowCount, b);
                var flatPartialC = MatrixOperator.flatten(partialC, 0, rowCount, rowSize);

                MPI.COMM_WORLD.Send(new int[]{rowCount}, 0, 1, MPI.INT, MASTER, META_TAG);
                MPI.COMM_WORLD.Send(flatPartialC, 0, rowCount * rowSize, MPI.INT, MASTER, C_TAG);
            }
        }

        MPI.Finalize();
    }
}