package prodcons;

public class Drop {
    private int number;
    private boolean empty = true;

    public synchronized int take() {
        while (empty) {
            try {
                wait();
            } catch (InterruptedException _) {}
        }
        empty = true;
        notifyAll();
        return number;
    }

    public synchronized void put(int number) {
        while (!empty) {
            try {
                wait();
            } catch (InterruptedException _) {}
        }
        empty = false;
        this.number = number;
        notifyAll();
    }
}
