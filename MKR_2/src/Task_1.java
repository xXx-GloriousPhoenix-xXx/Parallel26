static class Task_1 extends RecursiveTask<Double> {
    private static final int THRESHOLD = 1000;
    private final double[] array;
    private final int start;
    private final int end;

    public Task_1(double[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Double compute() {
        if (end - start <= THRESHOLD) {
            var sum = 0D;
            for (var i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        } else {
            var mid = start + (end - start) / 2;
            var leftTask = new Task_1(array, start, mid);
            var rightTask = new Task_1(array, mid, end);
            invokeAll(leftTask, rightTask);
            return leftTask.join() + rightTask.join();
        }
    }
}

void main() {
    var n = 100000;
    var numbers = new double[n];
    var random = new Random();

    for (var i = 0; i < n; i++) {
        numbers[i] = random.nextDouble(1, 1000);
    }

    try (var pool = new ForkJoinPool(12)) {
        var totalSum = pool.invoke(new Task_1(numbers, 0, n));
        IO.println("Result: " + totalSum);
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
}