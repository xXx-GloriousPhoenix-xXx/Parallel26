package lines;

public class UnsyncPrinter extends Printer {
    public UnsyncPrinter(int repeats_of_lines, int repeats_per_line) {
        super(repeats_of_lines, repeats_per_line);
    }

    @Override
    protected void printForward() {
        for (var i = 0; i < total_repeats; i++) {
            System.out.print('/');
            if (counter % (3 * repeats_per_line) == 0) {
                System.out.println();
            }
        }
    }

    @Override
    protected void printStraight() {
        for (var i = 0; i < total_repeats; i++) {
            System.out.print('|');
            counter++;
        }
    }

    @Override
    protected void printBackward() {
        for (var i = 0; i < total_repeats; i++) {
            System.out.print('\\');
        }
    }
}

