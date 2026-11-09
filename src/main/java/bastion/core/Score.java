package bastion.core;

/**
 * A deterministic quality metric comparing a model {@code prediction} against a
 * {@code reference} (the expected / ground-truth target).
 *
 * <p>Implementations return a score in {@code [0.0, 1.0]} unless they explicitly
 * document a wider range. Scores are always comparable: higher is better.</p>
 */
@FunctionalInterface
public interface Score {
    double score(String prediction, String reference);
}
