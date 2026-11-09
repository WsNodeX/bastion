package bastion.metrics;

import bastion.core.Score;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Token-level F1: the harmonic mean of precision and recall over the bag of
 * tokens in {@code prediction} versus {@code reference}. Robust to word order,
 * which makes it a better default than exact match for free-form answers.
 */
public final class F1Score implements Score {
    @Override
    public double score(String prediction, String reference) {
        List<String> p = Tokens.tokenize(prediction);
        List<String> r = Tokens.tokenize(reference);
        if (p.isEmpty() && r.isEmpty()) return 1.0;
        if (p.isEmpty() || r.isEmpty()) return 0.0;

        Map<String, Integer> rc = counts(r);
        Map<String, Integer> pc = counts(p);

        double tp = 0.0;
        for (var e : pc.entrySet()) {
            int matched = Math.min(e.getValue(), rc.getOrDefault(e.getKey(), 0));
            tp += matched;
        }
        double precision = tp / p.size();
        double recall = tp / r.size();
        if (precision + recall == 0.0) return 0.0;
        return 2 * precision * recall / (precision + recall);
    }

    private static Map<String, Integer> counts(List<String> tokens) {
        Map<String, Integer> m = new HashMap<>();
        for (String t : tokens) m.merge(t, 1, Integer::sum);
        return m;
    }
}
