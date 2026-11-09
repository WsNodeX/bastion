package bastion.guards;

import bastion.core.Guard;
import bastion.core.GuardResult;

import java.util.regex.Pattern;

/**
 * Fails when the prediction matches the forbidden regular expression.
 */
public final class RegexDenyGuard implements Guard {

    private final String name;
    private final Pattern pattern;

    public RegexDenyGuard(String name, String regex) {
        this.name = name;
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public GuardResult test(String prediction) {
        if (prediction == null) return GuardResult.ok(name);
        boolean blocked = pattern.matcher(prediction).find();
        return blocked ? GuardResult.fail(name, "matches forbidden pattern " + pattern.pattern())
                       : GuardResult.ok(name);
    }
}
