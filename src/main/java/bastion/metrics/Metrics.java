package bastion.metrics;

import bastion.core.Score;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The built-in catalogue of metrics. Every entry is a zero-argument, offline
 * {@link Score} so the whole framework runs without a network or a model backend.
 */
public final class Metrics {

    private Metrics() {}

    public static Map<String, Score> defaults() {
        Map<String, Score> m = new LinkedHashMap<>();
        m.put("exact_match", new ExactMatch());
        m.put("f1", new F1Score());
        m.put("bleu", new Bleu());
        m.put("rouge_l", new RougeL());
        m.put("cosine", new CosineSimilarity());
        m.put("levenshtein", new Levenshtein());
        m.put("contains", new Contains());
        m.put("numeric_match", new NumericMatch());
        m.put("json_equal", new JsonEqual());
        m.put("regex_match", new RegexMatch());
        return m;
    }

    public static Score get(String name) {
        Score s = defaults().get(name);
        if (s == null) throw new IllegalArgumentException("unknown metric: " + name);
        return s;
    }
}
