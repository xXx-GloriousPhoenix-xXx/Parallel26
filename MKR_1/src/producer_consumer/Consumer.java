package producer_consumer;

import java.util.ArrayList;
import java.util.Random;

public class Consumer implements Runnable {
    private final Drop drop;
    private static final int DELAY = 50;

    public Consumer(Drop drop) {
        this.drop = drop;
    }

    public void run() {
        Random random = new Random();
        var receivedData = new ArrayList<Object>();

        for (Object obj = drop.take(); obj != null; obj = drop.take()) {
            receivedData.add(obj);
            try {
                Thread.sleep(random.nextInt(DELAY));
            } catch (InterruptedException _) {}
        }

        System.out.println(receivedData);
    }
}