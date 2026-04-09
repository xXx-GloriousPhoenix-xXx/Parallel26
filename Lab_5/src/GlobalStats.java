import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GlobalStats {
    AtomicLong totalQueueSum = new AtomicLong(0);
    AtomicLong totalQueueCount = new AtomicLong(0);
    AtomicInteger totalRejected = new AtomicInteger(0);
    AtomicInteger totalRequests = new AtomicInteger(0);
}