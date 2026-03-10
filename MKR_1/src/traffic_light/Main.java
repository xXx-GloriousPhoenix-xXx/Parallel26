package traffic_light;

import java.util.concurrent.atomic.AtomicInteger;

public class TrafficLight {
    enum Signal { GREEN, YELLOW, RED }
    static volatile Signal signal = Signal.RED;
    static final AtomicInteger counter = new AtomicInteger(0);
    static final int MAX_VEHICLES = 10000;

    public static void main(String[] args) throws InterruptedException {
        var light = new Thread(() -> {
            try {
                while (counter.get() < MAX_VEHICLES) {
                    updateSignal(Signal.GREEN, 70);
                    updateSignal(Signal.YELLOW, 10);
                    updateSignal(Signal.RED, 40);
                    updateSignal(Signal.YELLOW, 10);
                }
            } catch (InterruptedException ignored) {}
        });

        var green = new Thread(new Runner(Signal.GREEN));
        var red = new Thread(new Runner(Signal.RED));

        light.start();
        green.start();
        red.start();

        light.join();
        green.join();
        red.join();
        System.out.println("Finished. Total: " + counter.get());
    }

    private static void updateSignal(Signal s, int ms) throws InterruptedException {
        signal = s;
        Thread.sleep(ms);
    }

    static class Runner implements Runnable {
        private final Signal permission;

        public Runner(Signal permission) { this.permission = permission; }

        @Override
        public void run() {
            try {
                while (counter.get() < MAX_VEHICLES) {
                    if (signal == permission) {
                        synchronized (TrafficSystem.class) {
                            if (signal == permission && counter.get() < MAX_VEHICLES) {
                                go();
                            }
                        }
                    }
                    Thread.sleep(400);
                }
            } catch (InterruptedException _) {}
        }

        private void go() throws InterruptedException {
            Thread.sleep(2);
            int current = counter.incrementAndGet();
            if (current % 1000 == 0) System.out.println("Проїхала: " + current);
        }
    }
}