package producer_consumer;

public class Main {
    static void main() {
        var drop = new Drop();
        var p1 = new Thread(new Producer(drop));
        var p2 = new Thread(new Producer(drop));
        var c = new Thread(new Consumer(drop));

        p1.start();
        p2.start();
        c.start();

        try {
            p1.join();
            p2.join();
            c.join();
        }
        catch (InterruptedException _) {}
    }
}



