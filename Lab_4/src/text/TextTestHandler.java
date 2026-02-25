package text;

import text.analyzer.TextAnalyzer;
import text.analyzer.TextStat;
import text.factory.LazyText;

import java.util.concurrent.ForkJoinPool;

public class TextTestHandler {
    public static void handle() throws InterruptedException {
        var repeats = 10;
        var sizes = new int[] {
                1_000_000,
                5_000_000,
                10_000_000,
                15_000_000,
                20_000_000,
                25_000_000,
                30_000_000,
                45_000_000,
                60_000_000
        };
        var maxWordLength = 16;

        System.out.println("Кількість слів | Sequential (мс) | ForkJoin (мс) | Прискорення");
        System.out.println("---------------|-----------------|---------------|------------");

        for (var words : sizes) {
            var totalSeq = 0L;
            var totalFj = 0L;
            var text = new LazyText(words, maxWordLength);

            var seqCombinedStat = new TextStat();
            var fjCombinedStat = new TextStat();

            for (var i = 0 ; i < repeats; i++) {
                System.gc();
                Thread.sleep(100);
                totalSeq += (long) handleSequential(text, seqCombinedStat);

                System.gc();
                Thread.sleep(100);
                totalFj += (long) handleForkJoin(text, fjCombinedStat);
            }

            var seqAvgTime = (double)totalSeq / repeats;
            var fJAvgTime = (double)totalFj / repeats;
            var speedup = seqAvgTime / fJAvgTime;


            System.out.printf("%-14d | %-15.3f | %-13.3f | %.2fx\n",
                    words, seqAvgTime, fJAvgTime, speedup);

            fjCombinedStat.dump();
            System.out.println();
        }
    }
    static double handleSequential(LazyText text, TextStat combinedStat) {
        var analyzer = new TextAnalyzer(text);

        var start = System.nanoTime();
        var stat = analyzer.performSequential();
        var end = System.nanoTime();

        combinedStat.combine(stat);

        return (end - start) / 1e6;
    }
    static double handleForkJoin(LazyText text, TextStat combinedStat) {
        var analyzer = new TextAnalyzer(text);
        ForkJoinPool pool = new ForkJoinPool();

        var start = System.nanoTime();
        var stat = pool.invoke(analyzer);
        var end = System.nanoTime();

        combinedStat.combine(stat);

        pool.shutdown();

        return (end - start) / 1e6;
    }
}
