package lines;

public class PrinterTest {
    private final int repeats_of_lines = 90;
    private final int repeats_per_line = 60;
    void main() {
        var isSynced = true;
        if (isSynced) {
            testSync();
        }
        else {
            testUnsync();
        }
    }

    void testUnsync() {
        System.out.println("===== Unsync =====");
        var unsyncPrinter = new UnsyncPrinter(repeats_of_lines, repeats_per_line);
        new Thread(unsyncPrinter::printStraight).start();
        new Thread(unsyncPrinter::printBackward).start();
        new Thread(unsyncPrinter::printForward).start();
    }

    void testSync() {
        System.out.println("===== Sync =====");
        var syncPrinter = new SyncPrinter(repeats_of_lines, repeats_per_line);
        new Thread(syncPrinter::printStraight).start();
        new Thread(syncPrinter::printBackward).start();
        new Thread(syncPrinter::printForward).start();
    }
}
