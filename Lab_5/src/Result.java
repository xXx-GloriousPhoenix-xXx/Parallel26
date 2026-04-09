public class Result {
    double avgQueueLength;
    double rejectionProbability;

    public Result(double avgQueueLength, double rejectionProbability) {
        this.avgQueueLength = avgQueueLength;
        this.rejectionProbability = rejectionProbability;
    }
}