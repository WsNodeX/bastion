package bastion.guards;

import bastion.core.Guard;
import bastion.core.GuardResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Combines several guards with boolean logic.
 *
 * <p>{@code allOf} requires every child guard to pass; {@code anyOf} passes when
 * at least one child passes. Used to build compound policies such as
 * "no PII AND no toxicity".</p>
 */
public final class CompositeGuard implements Guard {

    private final String name;
    private final boolean all;
    private final List<Guard> guards;

    public CompositeGuard(String name, boolean all, List<Guard> guards) {
        this.name = name;
        this.all = all;
        this.guards = List.copyOf(guards);
    }

    @Override
    public GuardResult test(String prediction) {
        StringBuilder detail = new StringBuilder();
        boolean result = all;
        for (Guard g : guards) {
            GuardResult r = g.test(prediction);
            if (all) {
                if (!r.passed()) {
                    result = false;
                    detail.append(r.name()).append(": ").append(r.detail()).append("; ");
                }
            } else {
                if (r.passed()) { result = true; break; }
                detail.append(r.name()).append(": ").append(r.detail()).append("; ");
            }
        }
        if (detail.length() == 0) {
            detail.append(all ? "all sub-guards passed" : "no sub-guard matched");
        }
        return new GuardResult(name, result, detail.toString());
    }
}
