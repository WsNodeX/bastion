package bastion.guards;

import bastion.core.Guard;
import bastion.core.GuardResult;

import java.util.regex.Pattern;

/**
 * Passes only when the prediction matches the required regular expression.
 */
public final class RegexAllowGuard implements Guard {

    private final String name;
    private final Pattern pattern;

    public RegexAllowGuard(String name, String regex) {
        this.name = name;
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public GuardResult test(String prediction) {
        if (prediction == null) return GuardResult.fail(name, "prediction is null");
        boolean ok = pattern.matcher(prediction).find();
        return ok ? GuardResult.ok(name)
                  : GuardResult.fail(name, "does not satisfy required pattern " + pattern.pattern());
    }
}
