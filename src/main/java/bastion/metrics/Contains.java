package bastion.metrics;

import bastion.core.Score;

/**
 * Substring containment: {@code 1.0} when the prediction contains the expected
 * {@code reference} text (case-insensitive, ignoring surrounding whitespace).
 */
public final class Contains implements Score {
    @Override
    public double score(String prediction, String reference) {
        if (prediction == null || reference == null) return 0.0;
        String p = prediction.toLowerCase().trim();
        String r = reference.toLowerCase().trim();
        if (r.isEmpty()) return p.isEmpty() ? 1.0 : 0.0;
        return p.contains(r) ? 1.0 : 0.0;
    }
}
