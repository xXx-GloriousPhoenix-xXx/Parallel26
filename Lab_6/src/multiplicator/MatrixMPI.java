package multiplicator;

import java.util.stream.IntStream;

public abstract class MatrixMPI {
    protected static final int MASTER = 0;
    protected static final int MIN = 1, MAX = 100;
    protected static final int[] SIZES = IntStream
            .iterate(500, i -> i + 500)
            .limit(5)
            .toArray();
    protected static final int REPEATS = 1;
}
