import mpi.MPI;
import java.util.Arrays;

void main(String[] args) {
    MPI.Init(args);

    var rank = MPI.COMM_WORLD.Rank();
    var size = MPI.COMM_WORLD.Size();

    var n = 100;
    var total = n * size;

    String[] A;

    var local = new String[n];

    String[] result = null;

    if (rank == 0) {
        A = new String[total];
        for (var i = 0; i < total; i++) {
            A[i] = "str" + (int)(Math.random() * 1000);
        }

        result = new String[size];

        MPI.COMM_WORLD.Scatter(
                A, 0, n, MPI.OBJECT,
                local, 0, n, MPI.OBJECT,
                0
        );
    } else {
        MPI.COMM_WORLD.Scatter(
                null, 0, n, MPI.OBJECT,
                local, 0, n, MPI.OBJECT,
                0
        );
    }

    Arrays.sort(local);

    var localMin = local[0];

    if (rank == 0) {
        MPI.COMM_WORLD.Gather(
                new String[]{localMin}, 0, 1, MPI.OBJECT,
                result, 0, 1, MPI.OBJECT,
                0
        );
    } else {
        MPI.COMM_WORLD.Gather(
                new String[]{localMin}, 0, 1, MPI.OBJECT,
                null, 0, 1, MPI.OBJECT,
                0
        );
    }

    if (rank == 0) {
        System.out.println("Result: " + Arrays.toString(result));
    }

    MPI.Finalize();
}