package bastion.examples;

import bastion.core.Example;
import bastion.core.Gate;
import bastion.core.GateResult;
import bastion.core.Guard;
import bastion.core.Report;
import bastion.core.Runner;
import bastion.core.ScoredExample;
import bastion.guards.Guards;
import bastion.judge.StubJudge;

import java.util.List;
import java.util.Map;

/**
 * End-to-end, zero-network showcase: scores a handful of answers with lexical
 * metrics, screens them with a PII guard, asks the offline judge for a verdict,
 * and finally applies a quality gate.
 */
public final class Demo {

    public static void main(String[] args) {
        List<Example> data = List.of(
                new Example("q1", "What is the capital of France?",
                        "The capital of France is Paris.",
                        "The capital of France is Paris."),
                new Example("q2", "What is 2 + 2?", "4",
                        "I don't know."),
                new Example("q3", "Summarise the paragraph.", "contact info",
                        "You can reach the team at bob@example.com for details."));

        Runner runner = new Runner()
                .withMetrics("exact_match", "f1", "rouge_l", "contains", "regex_match")
                .withGuards(Map.of("pii", Guards.defaults().get("pii")))
                .withJudge(new StubJudge());

        Report report = runner.run(data);
        System.out.println(report.toText());

        Gate gate = new Gate().requireMetric("f1", 0.5).requireGuard("pii");
        int passed = 0;
        for (ScoredExample se : report.items) {
            GateResult gr = gate.evaluate(se);
            if (gr.passed()) passed++;
            System.out.println("gate[" + se.example().id() + "]: " + (gr.passed() ? "PASS" : "FAIL"));
        }
        System.out.println("gate pass rate: " + passed + "/" + report.total);
    }
}
