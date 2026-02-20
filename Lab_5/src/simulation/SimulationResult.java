package simulation;

public record SimulationResult(double avgQueueLength, double rejectionProb, long totalArrivals, long totalRejected) { }

