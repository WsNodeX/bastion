package bastion.embed;

/**
 * Small dense-vector helpers used by the semantic-style metrics.
 */
public final class Vectors {

    private Vectors() {}

    public static double dot(double[] a, double[] b) {
        double sum = 0.0;
        int n = Math.min(a.length, b.length);
        for (int i = 0; i < n; i++) sum += a[i] * b[i];
        return sum;
    }

    public static double l2norm(double[] v) {
        double sum = 0.0;
        for (double x : v) sum += x * x;
        return Math.sqrt(sum);
    }

    /** Cosine similarity in {@code [-1, 1]}; returns 0 for degenerate zero vectors. */
    public static double cosine(double[] a, double[] b) {
        double na = l2norm(a);
        double nb = l2norm(b);
        if (na == 0.0 || nb == 0.0) return 0.0;
        return dot(a, b) / (na * nb);
    }
}
