package bastion.metrics;

import bastion.core.Score;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simplified BLEU score (n-grams up to order {@code maxOrder}, default 4) with
 * modified n-gram precision (counts clipped against the reference) and the
 * standard brevity penalty. Result is in {@code [0, 1]}.
 */
public final class Bleu implements Score {

    private final int maxOrder;

    public Bleu() { this(4); }

    public Bleu(int maxOrder) {
        if (maxOrder < 1) throw new IllegalArgumentException("maxOrder >= 1");
        this.maxOrder = maxOrder;
    }

    @Override
    public double score(String prediction, String reference) {
        List<String> p = Tokens.tokenize(prediction);
        List<String> r = Tokens.tokenize(reference);
        if (p.isEmpty()) return r.isEmpty() ? 1.0 : 0.0;

        double logSum = 0.0;
        int weightSum = 0;
        for (int n = 1; n <= maxOrder; n++) {
            Map<String, Integer> refCounts = counts(Tokens.ngrams(r, n));
            Map<String, Integer> predCounts = counts(Tokens.ngrams(p, n));
            if (predCounts.isEmpty()) continue;
            double totalPred = 0.0;
            for (int c : predCounts.values()) totalPred += c;
            double correct = 0.0;
            for (var e : predCounts.entrySet()) {
                int clipped = Math.min(e.getValue(), refCounts.getOrDefault(e.getKey(), 0));
                correct += clipped;
            }
            double precision = correct / totalPred;
            if (precision > 0.0) {
                logSum += Math.log(precision);
                weightSum += 1;
            }
        }
        if (weightSum == 0) return 0.0;

        double bp = brevityPenalty(p.size(), r.size());
        double geo = Math.exp(logSum / weightSum);
        return bp * geo;
    }

    private static double brevityPenalty(int predLen, int refLen) {
        if (predLen >= refLen) return 1.0;
        if (refLen == 0) return 0.0;
        return Math.exp(1.0 - ((double) refLen) / predLen);
    }

    private static Map<String, Integer> counts(List<String> grams) {
        Map<String, Integer> m = new HashMap<>();
        for (String g : grams) m.merge(g, 1, Integer::sum);
        return m;
    }
}
