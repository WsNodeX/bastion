package bastion.core;

/**
 * An output guardrail: a predicate over a model {@code prediction} that decides
 * whether the produced text is safe / acceptable to ship.
 *
 * <p>Guards are intentionally side-effect free and deterministic so they can run
 * in production hot paths and in unit tests alike.</p>
 */
@FunctionalInterface
public interface Guard {
    GuardResult test(String prediction);
}
