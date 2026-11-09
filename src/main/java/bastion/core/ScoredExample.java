package bastion.core;

import java.util.Map;

/**
 * An {@link Example} enriched with every computed signal: metric scores, guard
 * outcomes and an optional judge verdict.
 */
public record ScoredExample(Example example,
                            Map<String, Double> metricScores,
                            Map<String, GuardResult> guardResults,
                            Judgement judgement) {}
