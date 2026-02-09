package lines;

public class SyncPrinter extends Printer {
    private int turn = 1;

    public SyncPrinter(int repeats_of_lines, int repeats_per_line) {
        super(repeats_of_lines, repeats_per_line);
    }

    @Override
    protected synchronized void printForward() {
        try {
            for (var i = 0; i < total_repeats; i++) {
                while (turn != 3) wait();
                System.out.print('/');
                takeTurn();
                if (counter % repeats_per_line == 0) {
                    System.out.println();
                }
                notifyAll();
            }
        } catch (InterruptedException _) {}
    }

    @Override
    protected synchronized void printStraight() {
        try {
            for (var i = 0; i < total_repeats; i++) {
                while (turn != 1) wait();
                System.out.print('|');
                takeTurn();

                notifyAll();
            }
        } catch (InterruptedException _) {}
    }

    @Override
    protected synchronized void printBackward() {
        try {
            for (var i = 0; i < total_repeats; i++) {
                while (turn != 2) wait();
                System.out.print('\\');
                takeTurn();

                notifyAll();
            }
        } catch (InterruptedException _) {}
    }

    private synchronized void takeTurn() {
        if (turn < 3) {
            turn++;
        }
        else {
            turn = 1;
            counter++;
        }
    }
}