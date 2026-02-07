package counter;

public class CounterTest {
    public void main(String[] args) {
        var n = 100_000;

        System.out.println("=== Тест 1: Unsafe ===");
        handleTest(n, TestCase.Unsafe);

        System.out.println("\n=== Тест 2: Sync ===");
        handleTest(n, TestCase.Sync);

        System.out.println("\n=== Тест 3: SyncBlock ===");
        handleTest(n, TestCase.SyncBlock);

        System.out.println("\n=== Тест 4: SyncLock ===");
        handleTest(n, TestCase.SyncLock);
    }

    public static void handleTest(int n, TestCase tc) {
        var counter = new Counter();

        Runnable inc = getOperation(counter, tc, true);
        Runnable dec = getOperation(counter, tc, false);

        var uti = new Thread(() -> executeNTimes(n, inc));
        var utd = new Thread(() -> executeNTimes(n, dec));

        uti.start();
        utd.start();

        try {
            uti.join();
            utd.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Результат: " + counter.getValue());
    }

    private static Runnable getOperation(Counter counter, TestCase tc, boolean isIncrement) {
        return switch (tc) {
            case Unsafe -> isIncrement ?
                    counter::unsafeIncrement :
                    counter::unsafeDecrement;
            case Sync -> isIncrement ?
                    counter::synchronizedIncrement :
                    counter::synchronizedDecrement;
            case SyncBlock -> isIncrement ?
                    counter::synchronizedBlockIncrement :
                    counter::synchronizedBlockDecrement;
            case SyncLock -> isIncrement ?
                    counter::synchronizedLockIncrement :
                    counter::synchronizedLockDecrement;
        };
    }

    private static void executeNTimes(int n, Runnable operation) {
        for (var i = 0; i < n; i++) {
            operation.run();
        }
    }
}
