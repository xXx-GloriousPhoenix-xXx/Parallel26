package lines;

public abstract class Printer {
    protected final int repeats_of_lines;
    protected final int repeats_per_line;
    protected final int total_repeats;
    protected int counter = 0;
    public Printer(int repeats_of_lines, int repeats_per_line) {
        this.repeats_of_lines = repeats_of_lines;
        this.repeats_per_line = repeats_per_line;
        this.total_repeats = repeats_of_lines * repeats_per_line;
    }
    protected abstract void printForward();
    protected abstract void printStraight();
    protected abstract void printBackward();
}
