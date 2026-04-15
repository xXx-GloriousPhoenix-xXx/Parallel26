import mpi.MPI;

void main(String[] args) {
    MPI.Init(args);

    var rank = MPI.COMM_WORLD.Rank();
    var size = MPI.COMM_WORLD.Size();

    var n = 8;
    var rowsPerWorker = n / size;

    int[] A;
    int[] B;

    var localA = new int[rowsPerWorker * n];
    var localB = new int[rowsPerWorker * n];
    var localC = new int[rowsPerWorker];

    int[] C = null;

    if (rank == 0) {
        A = new int[n * n];
        B = new int[n * n];
        C = new int[n];

        for (int i = 0; i < n * n; i++) {
            A[i] = i + 1;
            B[i] = (i + 1) * 2;
        }

        MPI.COMM_WORLD.Scatter(
                A, 0, rowsPerWorker * n, MPI.INT,
                localA, 0, rowsPerWorker * n, MPI.INT,
                0
        );

        MPI.COMM_WORLD.Scatter(
                B, 0, rowsPerWorker * n, MPI.INT,
                localB, 0, rowsPerWorker * n, MPI.INT,
                0
        );

    } else {
        MPI.COMM_WORLD.Scatter(
                null, 0, rowsPerWorker * n, MPI.INT,
                localA, 0, rowsPerWorker * n, MPI.INT,
                0
        );

        MPI.COMM_WORLD.Scatter(
                null, 0, rowsPerWorker * n, MPI.INT,
                localB, 0, rowsPerWorker * n, MPI.INT,
                0
        );
    }

    for (var i = 0; i < rowsPerWorker; i++) {
        var sumA = 0;
        var sumB = 0;

        for (var j = 0; j < n; j++) {
            sumA += localA[i * n + j];
            sumB += localB[i * n + j];
        }

        var avgA = sumA / n;
        var avgB = sumB / n;

        localC[i] = (avgA * avgB);
    }

    if (rank == 0) {
        MPI.COMM_WORLD.Gather(
                localC, 0, rowsPerWorker, MPI.INT,
                C, 0, rowsPerWorker, MPI.INT,
                0
        );
    } else {
        MPI.COMM_WORLD.Gather(
                localC, 0, rowsPerWorker, MPI.INT,
                null, 0, rowsPerWorker, MPI.INT,
                0
        );
    }

    if (rank == 0) {
        IO.println("Result:");
        for (int i = 0; i < n; i++) {
            IO.print(C[i] + " ");
        }
    }

    MPI.Finalize();
}