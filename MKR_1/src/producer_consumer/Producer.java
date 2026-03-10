package producer_consumer;

import java.util.Random;

public class Producer implements Runnable {
    private final Drop drop;
    private static final int ITEMS = 100;
    private static final int MAX_VALUE = 1000;
    private static final int DELAY = 50;

    public Producer(Drop drop) {
        this.drop = drop;
    }

    public void run() {
        Random random = new Random();
        for (int i = 0; i < ITEMS; i++) {
            var object = "Object_" + i;
            drop.put(object);

            try {
                Thread.sleep(random.nextInt(DELAY));
            } catch (InterruptedException _) {}
        }

        drop.put(Integer.MIN_VALUE);
    }
}
