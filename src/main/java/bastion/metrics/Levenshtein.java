package bastion.metrics;

import bastion.core.Score;

/**
 * Normalised edit-distance similarity: {@code 1 - distance / max(len)}.
 * Useful for catching near-miss answers and typos in identifiers or numbers.
 */
public final class Levenshtein implements Score {
    @Override
    public double score(String prediction, String reference) {
        if (prediction == null || reference == null) return 0.0;
        int dist = distance(prediction, reference);
        int max = Math.max(prediction.length(), reference.length());
        if (max == 0) return 1.0;
        return 1.0 - ((double) dist) / max;
    }

    private static int distance(String a, String b) {
        int n = a.length(), m = b.length();
        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;
        for (int i = 1; i <= n; i++) {
            cur[0] = i;
            for (int j = 1; j <= m; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(prev[j] + 1, cur[j - 1] + 1), prev[j - 1] + cost);
            }
            int[] t = prev; prev = cur; cur = t;
        }
        return prev[m];
    }
}
