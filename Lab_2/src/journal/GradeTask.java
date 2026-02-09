package journal;

public class GradeTask {
    private GradeOperation gradeOperation;
    private boolean empty = true;

    public synchronized GradeOperation take() {
        while (empty) {
            try {
                wait();
            } catch (InterruptedException _) {}
        }
        empty = true;
        notifyAll();
        return gradeOperation;
    }

    public synchronized void put(GradeOperation gradeOperation) {
        while (!empty) {
            try {
                wait();
            } catch (InterruptedException _) {}
        }
        empty = false;
        this.gradeOperation = gradeOperation;
        notifyAll();
    }
}
