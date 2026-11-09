package bastion.metrics;

import bastion.core.Score;

/**
 * Strict normalized equality: both sides are trimmed and lower-cased before
 * comparison. Returns {@code 1.0} on a match, otherwise {@code 0.0}.
 */
public final class ExactMatch implements Score {
    @Override
    public double score(String prediction, String reference) {
        if (prediction == null || reference == null) return 0.0;
        return normalize(prediction).equals(normalize(reference)) ? 1.0 : 0.0;
    }

    static String normalize(String s) {
        return s.trim().toLowerCase();
    }
}
