package switcher;

public class State {
    volatile String state = "r";
    boolean stop = false;
}

public class Main {
    public static void main() {
        var state = new State();

        var t1 = new Thread(() -> {
            while (!state.stop) {
                synchronized (state) {
                    state.state = state.state.equals("r") ? "w" : "r";
                    state.notifyAll();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        var t2 = new Thread(() -> {
            int countdown = 1000;
            while (countdown > 0 && !state.stop) {
                synchronized (state) {
                    while (!state.state.equals("r") && !state.stop) {
                        try {
                            state.wait();
                        } catch (InterruptedException e) {}
                    }
                }
                countdown -= 100;

                if (state.state.equals("w")) {
                    synchronized (state) {
                        try {
                            state.wait();
                        } catch (InterruptedException _) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }

            state.stop = true;
            synchronized (state) {
                state.notifyAll();
            }
        });

        t1.start();
        t2.start();
    }
}
