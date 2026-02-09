package prodcons;

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
        var receivedData = new ArrayList<Integer>();

        for (int number = drop.take(); number != Integer.MIN_VALUE; number = drop.take()) {
            System.out.format("NUMBER RECEIVED: %s%n", number);
            receivedData.add(number);

            try {
                Thread.sleep(random.nextInt(DELAY));
            } catch (InterruptedException _) {}
        }

        System.out.println(receivedData);
    }
}