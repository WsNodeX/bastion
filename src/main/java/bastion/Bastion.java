package bastion;

import bastion.core.Example;
import bastion.core.Report;
import bastion.core.Runner;
import bastion.core.Score;
import bastion.metrics.Metrics;

import java.util.List;
import java.util.Map;

/**
 * Top-level entry point and fluent facade for Bastion.
 *
 * <p>Most users only need {@link #runner()} to compose a {@link Runner}, or the
 * one-shot {@link #evaluate(List, String...)} convenience when the full metric
 * suite is acceptable.</p>
 */
public final class Bastion {

    private Bastion() {}

    public static Runner runner() {
        return new Runner();
    }

    public static Map<String, Score> defaultMetrics() {
        return Metrics.defaults();
    }

    public static Report evaluate(List<Example> examples, String... metricNames) {
        Runner r = new Runner();
        if (metricNames.length > 0) r.withMetrics(metricNames);
        return r.run(examples);
    }
}
