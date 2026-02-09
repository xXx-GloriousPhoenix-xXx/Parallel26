package bank;

public class TransferThread extends Thread {
    private final Bank bank;
    private final int fromAccount;
    private final int maxAmount;
    private static final int REPS = 1000;
    private final int mode; // 0 = sync method, 1 = sync block, 2 = lock

    public TransferThread(Bank b, int from, int max, int mode) {
        bank = b;
        fromAccount = from;
        maxAmount = max;
        this.mode = mode;
    }

    @Override
    public void run() {
        for (int iteration = 0; iteration < 100; iteration++) {
            for (int i = 0; i < REPS; i++) {
                int toAccount = (int) (bank.size() * Math.random());
                int amount = (int) (maxAmount * Math.random() / REPS);

                if (amount > 0) {
                    switch (mode) {
                        case 0 -> bank.synchronizedTransfer(fromAccount, toAccount, amount);
                        case 1 -> bank.synchronizedBlockTransfer(fromAccount, toAccount, amount);
                        case 2 -> bank.synchronizedLockTransfer(fromAccount, toAccount, amount);
                    }
                }
            }
        }
    }
}