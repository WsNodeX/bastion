package bastion.core;

/**
 * An LLM-as-judge abstraction.
 *
 * <p>A {@code Judge} grades a {@code prediction} given the original {@code input}.
 * The default offline {@code StubJudge} needs no network; production deployments
 * can plug a real model by implementing this single method.</p>
 */
@FunctionalInterface
public interface Judge {
    Judgement judge(String input, String prediction);
}
