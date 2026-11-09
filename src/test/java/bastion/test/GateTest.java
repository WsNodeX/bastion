package bastion.test;

import bastion.core.Example;
import bastion.core.Gate;
import bastion.core.GateResult;
import bastion.core.GuardResult;
import bastion.core.ScoredExample;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GateTest {

    private ScoredExample scored(double f1, boolean piiOk) {
        Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put("f1", f1);
        Map<String, GuardResult> guards = new LinkedHashMap<>();
        guards.put("pii", piiOk ? GuardResult.ok("pii") : GuardResult.fail("pii", "email"));
        return new ScoredExample(new Example("q", "i", "r", "p"), metrics, guards, null);
    }

    @Test
    void passesWhenAllMet() {
        Gate g = new Gate().requireMetric("f1", 0.5).requireGuard("pii");
        GateResult res = g.evaluate(scored(0.9, true));
        assertTrue(res.passed());
    }

    @Test
    void failsOnMetricThreshold() {
        Gate g = new Gate().requireMetric("f1", 0.9);
        GateResult res = g.evaluate(scored(0.5, true));
        assertFalse(res.passed());
        assertTrue(res.reasons().containsKey("f1"));
    }

    @Test
    void failsOnGuard() {
        Gate g = new Gate().requireGuard("pii");
        GateResult res = g.evaluate(scored(1.0, false));
        assertFalse(res.passed());
    }
}
