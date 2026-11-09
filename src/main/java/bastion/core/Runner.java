package bastion.core;

import bastion.metrics.Metrics;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Drives evaluation: for every {@link Example} it computes each configured
 * metric, runs each guard against the prediction, and optionally asks a
 * {@link Judge} for a verdict. The result is a single {@link Report}.
 */
public final class Runner {

    private Map<String, Score> metrics = Metrics.defaults();
    private Map<String, Guard> guards = Map.of();
    private Judge judge = null;

    public Runner withMetrics(Map<String, Score> metrics) {
        this.metrics = new LinkedHashMap<>(metrics);
        return this;
    }

    /** Select a named subset of the built-in metrics (in the given order). */
    public Runner withMetrics(String... names) {
        Map<String, Score> all = Metrics.defaults();
        Map<String, Score> chosen = new LinkedHashMap<>();
        for (String n : names) {
            Score s = all.get(n);
            if (s == null) throw new IllegalArgumentException("unknown metric: " + n);
            chosen.put(n, s);
        }
        this.metrics = chosen;
        return this;
    }

    public Runner withGuards(Map<String, Guard> guards) {
        this.guards = new LinkedHashMap<>(guards);
        return this;
    }

    public Runner withJudge(Judge judge) {
        this.judge = judge;
        return this;
    }

    public Report run(List<Example> examples) {
        List<ScoredExample> out = new java.util.ArrayList<>();
        for (Example ex : examples) {
            Map<String, Double> ms = new LinkedHashMap<>();
            for (var e : metrics.entrySet()) {
                ms.put(e.getKey(), e.getValue().score(ex.prediction(), ex.reference()));
            }
            Map<String, GuardResult> gr = new LinkedHashMap<>();
            for (var e : guards.entrySet()) {
                gr.put(e.getKey(), e.getValue().test(ex.prediction()));
            }
            Judgement j = judge == null ? null : judge.judge(ex.input(), ex.prediction());
            out.add(new ScoredExample(ex, ms, gr, j));
        }
        return new Report(out);
    }
}
