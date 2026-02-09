package bank;

class AsyncBankTest {
    public static final int ACCOUNTS = 10;
    public static final int INITIAL_BALANCE = 10000;

    void main() {
        System.out.println("=== Тест 1: Sync Method ===");
        Bank bank1 = new Bank(ACCOUNTS, INITIAL_BALANCE);
        for (int i = 0; i < ACCOUNTS; i++) {
            new TransferThread(bank1, i, INITIAL_BALANCE, 0).start();
        }

        try { Thread.sleep(2000); } catch (InterruptedException _) {}

        System.out.println("\n=== Тест 2: Sync Block ===");
        Bank bank2 = new Bank(ACCOUNTS, INITIAL_BALANCE);
        for (int i = 0; i < ACCOUNTS; i++) {
            new TransferThread(bank2, i, INITIAL_BALANCE, 1).start();
        }

        try { Thread.sleep(2000); } catch (InterruptedException _) {}

        System.out.println("\n=== Тест 3: Sync Lock ===");
        Bank bank3 = new Bank(ACCOUNTS, INITIAL_BALANCE);
        for (int i = 0; i < ACCOUNTS; i++) {
            new TransferThread(bank3, i, INITIAL_BALANCE, 2).start();
        }
    }
}