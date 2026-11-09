package bastion.core;

/**
 * Verdict produced by a {@link Judge}.
 *
 * @param passed    whether the prediction is acceptable
 * @param score     quality estimate in {@code [0.0, 1.0]}
 * @param rationale short explanation, surfaced in reports
 */
public record Judgement(boolean passed, double score, String rationale) {}
