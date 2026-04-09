public class TheoreticalModel {
    public static Result calculate(int c, int K, double lambda, double mu) {
        var maxState = c + K;
        var a = lambda / mu;
        var P = new double[maxState + 1];
        var sum = 0.0;
        for (var n = 0; n <= maxState; n++) {
            sum += stateProbabilityRaw(n, c, a);
        }
        var P0 = 1.0 / sum;
        for (var n = 0; n <= maxState; n++) {
            P[n] = stateProbabilityRaw(n, c, a) * P0;
        }
        var rejectProb = P[maxState];
        var Lq = 0.0;
        for (var n = c; n <= maxState; n++) {
            Lq += (n - c) * P[n];
        }
        return new Result(Lq, rejectProb);
    }

    private static double stateProbabilityRaw(int n, int c, double a) {
        if (n <= c) {
            return Math.pow(a, n) / factorial(n);
        } else {
            return Math.pow(a, n) / (factorial(c) * Math.pow(c, n - c));
        }
    }

    private static double factorial(int n) {
        var res = 1.0;
        for (var i = 2; i <= n; i++) res *= i;
        return res;
    }
}