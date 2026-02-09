package counter;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Counter {
    private int value = 0;
    private final Lock lock = new ReentrantLock();

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
        lock.lock();
        try {
            value++;
        }
        finally {
            lock.unlock();
        }
    }
    public void synchronizedLockDecrement() {
        lock.lock();
        try {
            value--;
        }
        finally {
            lock.unlock();
        }
    }

    public int getValue() { return value; }
}
