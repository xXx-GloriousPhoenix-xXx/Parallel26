import java.util.concurrent.ForkJoinPool;
import java.util.List;
import java.util.concurrent.RecursiveTask;

static class Task_2 extends RecursiveTask<Result> {
    private final List<String> list;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 5000;

    public Task_2(List<String> list, int start, int end) {
        this.list = list;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Result compute() {
        if (end - start <= THRESHOLD) {
            var chars = 0;
            var words = 0;
            for (int i = start; i < end; i++) {
                String word = list.get(i);
                if (word != null) {
                    chars += word.length();
                    words++;
                }
            }
            return new Result(chars, words);
        } else {
            var mid = start + (end - start) / 2;
            var left = new Task_2(list, start, mid);
            var right = new Task_2(list, mid, end);
            invokeAll(left, right);
            return Result.combine(left.join(), right.join());
        }
    }
}

public record Result(long totalChars, long totalWords) {
    public static Result combine(Result r1, Result r2) {
        return new Result(r1.totalChars + r2.totalChars, r1.totalWords + r2.totalWords);
    }
}

public void main() {
    var list = new ArrayList<String>();

    for (int i = 0; i < 100_000; i++) {
        list.add("word" + i);
    }

    try (var pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors())) {
        var finalResult = pool.invoke(
                new Task_2(list, 0, list.size())
        );

        if (finalResult.totalWords() > 0) {
            var average = (double) finalResult.totalChars() / finalResult.totalWords();
            IO.println("Average word length: " + average);
        } else {
            IO.println("No words found");
        }

    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
}