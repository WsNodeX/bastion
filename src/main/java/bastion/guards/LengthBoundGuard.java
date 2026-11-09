package bastion.guards;

import bastion.core.Guard;
import bastion.core.GuardResult;

/**
 * Enforces that the prediction length (in characters) stays within an inclusive
 * {@code [min, max]} window. A {@code null} bound means "unbounded" on that side.
 */
public final class LengthBoundGuard implements Guard {

    private final String name;
    private final Integer min;
    private final Integer max;

    public LengthBoundGuard(String name, Integer min, Integer max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }

    @Override
    public GuardResult test(String prediction) {
        int len = prediction == null ? 0 : prediction.length();
        if (min != null && len < min) return GuardResult.fail(name, "length " + len + " < min " + min);
        if (max != null && len > max) return GuardResult.fail(name, "length " + len + " > max " + max);
        return GuardResult.ok(name);
    }
}
