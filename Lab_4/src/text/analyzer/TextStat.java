package text.analyzer;

public class TextStat {
    public int min = Integer.MAX_VALUE;
    public int max = Integer.MIN_VALUE;
    public double avg = 0;
    public int count = 0;
    public long total = 0;

    public TextStat() {}

    public void dump() {
        System.out.format("[%d; %d] with avg: %2f\n", min, max, avg);
    }

    public TextStat combine(TextStat other) {
        this.min = Math.min(this.min, other.min);
        this.max = Math.max(this.max, other.max);
        this.count += other.count;
        this.total += other.total;
        this.avg = (double)this.total / this.count;

        return this;
    }
}
