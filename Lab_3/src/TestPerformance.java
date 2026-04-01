import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

static int MIN = 1;
static int MAX = 10;
final int[] SIZES = new int[] { 510, 1020, 1500, 2010, 2490, 3000 };
final int[] THREADS = new int[] { 4, 9, 25 };
final int REPEATS = 5;

void main() {
    var sizeCount = SIZES.length;
    var seqtimes = new long[sizeCount];

    var ribtimes4 = new long[sizeCount];
    var ribtimes9 = new long[sizeCount];
    var ribtimes25 = new long[sizeCount];

    var foxtimes4 = new long[sizeCount];
    var foxtimes9 = new long[sizeCount];
    var foxtimes25 = new long[sizeCount];

    var csvFilePath = "./Lab_3/src/Result.csv";

    try (var csvWriter = new PrintWriter(new FileWriter(csvFilePath))) {
        csvWriter.println("Size,SeqTime_ms,RibTime4_ms,RibSpeedup4,RibTime9_ms,RibSpeedup9,RibTime25_ms,RibSpeedup25,FoxTime4_ms,FoxSpeedup4,FoxTime9_ms,FoxSpeedup9,FoxTime25_ms,FoxSpeedup25");

        System.out.printf("%-8s %-12s %-12s %-12s %-12s %-12s %-12s %-12s %-12s %-12s %-12s %-12s %-12s %-12s%n",
                "Size", "SeqTime_ms", "RibTime4_ms", "RibSpeedup4", "RibTime9_ms", "RibSpeedup9",
                "RibTime25_ms", "RibSpeedup25   ", "FoxTime4_ms", "FoxSpeedup4", "FoxTime9_ms",
                "FoxSpeedup9", "FoxTime25_ms", "FoxSpeedup25");
        System.out.println("-".repeat(180));

        for (var s = 0; s < sizeCount; s++) {
            var size = SIZES[s];
            var seqtime = 0L;

            var ribtime4 = 0L;
            var ribtime9 = 0L;
            var ribtime25 = 0L;

            var foxtime4 = 0L;
            var foxtime9 = 0L;
            var foxtime25 = 0L;

            for (var r = 0; r < REPEATS; r++) {
                var a = new Matrix(size, MIN, MAX);
                var b = new Matrix(size, MIN, MAX);

                // Sequential
                var start = System.nanoTime();
                var _ = a.multiplySequential(b);
                var end = System.nanoTime();
                seqtime += (end - start);

                // Ribbon with different thread counts
                start = System.nanoTime();
                var _ = a.multiplyRibbon(b, 4);
                end = System.nanoTime();
                ribtime4 += (end - start);

                start = System.nanoTime();
                var _ = a.multiplyRibbon(b, 9);
                end = System.nanoTime();
                ribtime9 += (end - start);

                start = System.nanoTime();
                var _ = a.multiplyRibbon(b, 25);
                end = System.nanoTime();
                ribtime25 += (end - start);

                // Fox with different thread counts
                start = System.nanoTime();
                var _ = a.multiplyFox(b, 4);
                end = System.nanoTime();
                foxtime4 += (end - start);

                start = System.nanoTime();
                var _ = a.multiplyFox(b, 9);
                end = System.nanoTime();
                foxtime9 += (end - start);

                start = System.nanoTime();
                var _ = a.multiplyFox(b, 25);
                end = System.nanoTime();
                foxtime25 += (end - start);
            }

            seqtimes[s] = seqtime / REPEATS / 1_000_000;
            ribtimes4[s] = ribtime4 / REPEATS / 1_000_000;
            ribtimes9[s] = ribtime9 / REPEATS / 1_000_000;
            ribtimes25[s] = ribtime25 / REPEATS / 1_000_000;
            foxtimes4[s] = foxtime4 / REPEATS / 1_000_000;
            foxtimes9[s] = foxtime9 / REPEATS / 1_000_000;
            foxtimes25[s] = foxtime25 / REPEATS / 1_000_000;

            double ribSpeedup4 = (double) seqtimes[s] / ribtimes4[s];
            double ribSpeedup9 = (double) seqtimes[s] / ribtimes9[s];
            double ribSpeedup25 = (double) seqtimes[s] / ribtimes25[s];

            double foxSpeedup4 = (double) seqtimes[s] / foxtimes4[s];
            double foxSpeedup9 = (double) seqtimes[s] / foxtimes9[s];
            double foxSpeedup25 = (double) seqtimes[s] / foxtimes25[s];

            System.out.printf("%-8d %-12d %-12d %-12.2f %-12d %-12.2f %-12d %-12.2f %-12d %-12.2f %-12d %-12.2f %-12d %-12.2f%n",
                    size, seqtimes[s],
                    ribtimes4[s], ribSpeedup4,
                    ribtimes9[s], ribSpeedup9,
                    ribtimes25[s], ribSpeedup25,
                    foxtimes4[s], foxSpeedup4,
                    foxtimes9[s], foxSpeedup9,
                    foxtimes25[s], foxSpeedup25);

            csvWriter.printf("%d,%d,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f%n",
                    size, seqtimes[s],
                    ribtimes4[s], ribSpeedup4,
                    ribtimes9[s], ribSpeedup9,
                    ribtimes25[s], ribSpeedup25,
                    foxtimes4[s], foxSpeedup4,
                    foxtimes9[s], foxSpeedup9,
                    foxtimes25[s], foxSpeedup25);
            csvWriter.flush();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}