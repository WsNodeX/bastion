package bastion.core;

/**
 * Outcome of evaluating a single {@link Guard} against a prediction.
 *
 * @param name    stable identifier of the guard (used in reports and gates)
 * @param passed  {@code true} when the prediction satisfied the guard
 * @param detail  human readable reason, useful when {@code passed} is false
 */
public record GuardResult(String name, boolean passed, String detail) {
    public static GuardResult ok(String name) {
        return new GuardResult(name, true, "passed");
    }

    public static GuardResult fail(String name, String detail) {
        return new GuardResult(name, false, detail);
    }
}
