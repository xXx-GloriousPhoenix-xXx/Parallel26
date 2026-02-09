package prodcons;

class ProducerConsumerExample {
    void main() {
        Drop drop = new Drop();
        (new Thread(new Producer(drop))).start();
        (new Thread(new Consumer(drop))).start();
    }
}