package bank;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Bank {
    public static final int NTEST = 10000;
    private final int[] accounts;
    private long transacts;
    private final Lock lock = new ReentrantLock();
    public Bank(int n, int initialBalance){
        accounts = new int[n];
        Arrays.fill(accounts, initialBalance);
        transacts = 0;
    }
    public synchronized void synchronizedTransfer(int from, int to, int amount) {
        accounts[from] -= amount;
        accounts[to] += amount;
        transacts++;
        if (transacts % NTEST == 0)
        {
            test();
        }
    }
    public void synchronizedBlockTransfer(int from, int to, int amount) {
        synchronized (this) {
            accounts[from] -= amount;
            accounts[to] += amount;
            transacts++;
            if (transacts % NTEST == 0)
            {
                test();
            }
        }
    }
    public void synchronizedLockTransfer(int from, int to, int amount) {
        lock.lock();
        try {
            accounts[from] -= amount;
            accounts[to] += amount;
            transacts++;
            if (transacts % NTEST == 0)
            {
                test();
            }
        }
        finally {
            lock.unlock();
        }
    }

    public void test(){
        int sum = 0;
        for (int account : accounts) sum += account;
        System.out.println("Transactions:" + transacts + " Sum: " + sum);
    }
    public int size(){
        return accounts.length;
    }
}