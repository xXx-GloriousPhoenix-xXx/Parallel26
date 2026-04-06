public class TestValues {
    public void main() {
        checkFox();
    }
    public void checkEqual() {
        var a = new Matrix(16, 1, 5);
        a.display();
        IO.println();

        var b = new Matrix(16, 1, 5);
        b.display();
        IO.println();

        var rseq = a.multiplySequential(b);
        var rrib = a.multiplyRibbon(b, 4);
        var rfox = a.multiplyFox(b, 4);
        rseq.display();
        IO.println();

        IO.println("Ribbon: " + rseq.isEqual(rrib));
        IO.println("Fox: " + rfox.isEqual(rfox));
    }
    public void checkFox() {
        final var SIZES = new int[] { 510, 1020, 1500, 2010, 2490, 3000 };
        final var THREADS = new int[] { 4, 9, 25 };
        IO.println("Size / Threads / Time(ms)");
        for (var size : SIZES) {
            var a = new Matrix(size, 1, 100);
            var b = new Matrix(size, 1, 100);
            for (var t : THREADS) {
                var start = System.nanoTime();
                var _ = a.multiplyFox(b, t);
                var end = System.nanoTime();
                IO.println(size + " " + t + " " + (end - start) / 1_000_000);
            }
        }
    }
}
