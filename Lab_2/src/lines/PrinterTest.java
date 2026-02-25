package lines;

public class PrinterTest {
    private static final int repeats_of_lines = 90;
    private static final int repeats_per_line = 60;
    void main() {
        test(false);
        test(true);
    }

    void test(boolean isSynced) {
        Printer p;
        if (isSynced) {
            System.out.println("===== Sync =====");
            p = new SyncPrinter(repeats_of_lines, repeats_per_line);
        }
        else {
            System.out.println("===== Unsync =====");
            p = new UnsyncPrinter(repeats_of_lines, repeats_per_line);
        }

        var threads = new Thread[] {
            new Thread(p::printStraight),
            new Thread(p::printBackward),
            new Thread(p::printForward)
        };

        for (var t : threads) {
            t.start();
        }

        try {
            for (var t : threads) {
                t.join();
            }
        }
        catch (InterruptedException _) {}
    }
}
