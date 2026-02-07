package counter;

public class Counter {
    private int value = 0;
    private final Object lock = new Object();

    public void unsafeIncrement() { value++; }
    public void unsafeDecrement() { value--; }

    public synchronized void synchronizedIncrement() { value++; }
    public synchronized void synchronizedDecrement() { value--; }

    public void synchronizedBlockIncrement() {
        synchronized (this) {
            value++;
        }
    }
    public void synchronizedBlockDecrement() {
        synchronized (this) {
            value--;
        }
    }

    public void synchronizedLockIncrement() {
        synchronized (lock) {
            value++;
        }
    }
    public void synchronizedLockDecrement() {
        synchronized (lock) {
            value--;
        }
    }

    public int getValue() { return value; }
}
