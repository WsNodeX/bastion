package bastion.metrics;

import bastion.core.Score;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compares the first number extracted from each side within a tolerance.
 *
 * <p>By default an absolute tolerance of {@code 1e-6} is used; pass a relative
 * tolerance via {@link #NumericMatch(double)} to compare large magnitudes
 * (e.g. currency, distances) proportionally.</p>
 */
public final class NumericMatch implements Score {

    private static final Pattern NUM = Pattern.compile("-?\\d+(?:\\.\\d+)?");
    private final double relTol;

    public NumericMatch() { this(1e-6); }

    public NumericMatch(double relTol) {
        this.relTol = relTol;
    }

    @Override
    public double score(String prediction, String reference) {
        Double a = first(prediction);
        Double b = first(reference);
        if (a == null || b == null) return 0.0;
        double tol = Math.max(relTol, relTol * Math.abs(b));
        return Math.abs(a - b) <= tol ? 1.0 : 0.0;
    }

    private static Double first(String s) {
        if (s == null) return null;
        Matcher m = NUM.matcher(s);
        return m.find() ? Double.parseDouble(m.group()) : null;
    }
}
