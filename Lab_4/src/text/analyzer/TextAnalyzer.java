package text.analyzer;

import text.factory.LazyText;

import java.util.concurrent.RecursiveTask;

public class TextAnalyzer extends RecursiveTask<TextStat> {
    private final LazyText text;
    public final int start;
    public final int end;
    private static final int THRESHOLD = 100_000;

    public TextAnalyzer(LazyText text, int start, int end) {
        this.text = text;
        this.start = start;
        this.end = end;
    }

    public TextAnalyzer(LazyText text) {
        this(text, 0, text.getSize());
    }

    public TextStat performSequential() {
        var stat = new TextStat();
        for (int i = start; i < end; i++) {
            var word = text.getWord(i);
            var length = word.length();
            stat.min = Math.min(stat.min, length);
            stat.max = Math.max(stat.max, length);
            stat.total += length;
            stat.count++;
        }
        stat.avg = (double)stat.total / stat.count;
        return stat;
    }

    @Override
    protected TextStat compute() {
        var length = end - start;
        if (length < THRESHOLD) {
            var stat = new TextStat();
            for (int i = start; i < end; i++) {
                var word = text.getWord(i);
                var wordLength = word.length();
                stat.min = Math.min(stat.min, wordLength);
                stat.max = Math.max(stat.max, wordLength);
                stat.total += wordLength;
                stat.count++;
            }
            stat.avg = (double)stat.total / stat.count;
            return stat;
        }

        var mid = start + length / 2;
        var left = new TextAnalyzer(text, start, mid);
        var right = new TextAnalyzer(text, mid, end);

        left.fork();
        var rightResult = right.compute();
        var leftResult = left.join();

        return leftResult.combine(rightResult);
    }
}