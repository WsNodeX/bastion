package bastion.metrics;

import bastion.core.Score;

import java.util.List;

/**
 * ROUGE-L: F1 based on the Longest Common Subsequence between the prediction and
 * the reference token sequences. LCS captures sentence-level structure better than
 * raw n-gram overlap.
 */
public final class RougeL implements Score {
    @Override
    public double score(String prediction, String reference) {
        List<String> p = Tokens.tokenize(prediction);
        List<String> r = Tokens.tokenize(reference);
        if (p.isEmpty() && r.isEmpty()) return 1.0;
        if (p.isEmpty() || r.isEmpty()) return 0.0;

        int lcs = lcsLength(p, r);
        double recall = (double) lcs / r.size();
        double precision = (double) lcs / p.size();
        if (precision + recall == 0.0) return 0.0;
        return 2 * precision * recall / (precision + recall);
    }

    private static int lcsLength(List<String> a, List<String> b) {
        int n = a.size(), m = b.size();
        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.get(i - 1).equals(b.get(j - 1))) cur[j] = prev[j - 1] + 1;
                else cur[j] = Math.max(prev[j], cur[j - 1]);
            }
            int[] t = prev; prev = cur; cur = t;
        }
        return prev[m];
    }
}
