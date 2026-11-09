package bastion.test;

import bastion.core.Example;
import bastion.core.Report;
import bastion.core.Runner;
import bastion.guards.Guards;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RunnerTest {

    @Test
    void runsSelectedMetrics() {
        Runner r = new Runner().withMetrics("exact_match", "f1");
        Report rep = r.run(List.of(
                new Example("q1", "the cat sat", "the cat sat"),
                new Example("q2", "dog", "cat")));
        assertEquals(2, rep.items.size());
        assertEquals(1.0, rep.items.get(0).metricScores().get("exact_match"));
        assertEquals(1.0, rep.items.get(0).metricScores().get("f1"));
        assertEquals(0.0, rep.items.get(1).metricScores().get("exact_match"));
        // means
        assertEquals(0.5, rep.mean("exact_match"), 1e-9);
    }

    @Test
    void runsGuards() {
        Runner r = new Runner().withGuards(Map.of("pii", Guards.defaults().get("pii")));
        Report rep = r.run(List.of(new Example("q", "x", "y", "email a@b.com")));
        assertFalse(rep.items.get(0).guardResults().get("pii").passed());
    }

    @Test
    void runsJudge() {
        Runner r = new Runner().withJudge((input, pred) ->
                new bastion.core.Judgement(pred.length() > 3, 1.0, "ok"));
        Report rep = r.run(List.of(new Example("q", "x", "y", "substantive answer")));
        assertTrue(rep.items.get(0).judgement().passed());
    }
}
