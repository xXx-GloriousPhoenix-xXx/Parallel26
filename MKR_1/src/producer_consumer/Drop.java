package producer_consumer;

public class Drop {
    private Object item;
    private boolean empty = true;

    public synchronized Object take() {
        while (empty) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        var takenItem = this.item;
        empty = true;
        notifyAll();
        return takenItem;
    }

    public synchronized void put(Object item) {
        if (item == null) {
            while (!empty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            this.item = null;
            empty = false;
            notifyAll();
            return;
        }

        while (!empty) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        this.item = item;
        empty = false;
        notifyAll();
    }
}
