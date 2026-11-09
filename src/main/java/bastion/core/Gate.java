package bastion.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A quality gate: a declarative set of minimum metric thresholds and required
 * guards. Call {@link #evaluate(ScoredExample)} to decide whether a single
 * prediction may ship. Gates turn raw scores into a binary release decision.
 */
public final class Gate {

    private final Map<String, Double> metricThresholds = new LinkedHashMap<>();
    private final List<String> requiredGuards = new ArrayList<>();

    public Gate requireMetric(String name, double min) {
        metricThresholds.put(name, min);
        return this;
    }

    public Gate requireGuard(String name) {
        requiredGuards.add(name);
        return this;
    }

    public GateResult evaluate(ScoredExample se) {
        Map<String, String> reasons = new LinkedHashMap<>();
        boolean ok = true;

        for (var e : metricThresholds.entrySet()) {
            Double v = se.metricScores().get(e.getKey());
            if (v == null || v < e.getValue()) {
                ok = false;
                reasons.put(e.getKey(), "score " + v + " < " + e.getValue());
            } else {
                reasons.put(e.getKey(), "ok (" + v + ")");
            }
        }
        for (String g : requiredGuards) {
            GuardResult gr = se.guardResults().get(g);
            if (gr == null || !gr.passed()) {
                ok = false;
                reasons.put("guard:" + g, gr == null ? "missing" : "fail: " + gr.detail());
            } else {
                reasons.put("guard:" + g, "ok");
            }
        }
        return new GateResult(ok, reasons);
    }
}
