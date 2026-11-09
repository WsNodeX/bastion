package bastion.guards;

import bastion.core.Guard;
import bastion.core.GuardResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects common personally-identifiable information using conservative
 * heuristics: email addresses, mainland-China mobile numbers, credit-card-like
 * digit runs, and 18-digit China resident ID numbers. Fails (blocks) on a match.
 *
 * <p>The patterns are intentionally broad to err on the side of caution; they are
 * a screening layer, not a compliant DLP solution.</p>
 */
public final class PiiGuard implements Guard {

    private final String name;

    private static final Pattern EMAIL =
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE =
            Pattern.compile("(?<![0-9])1[3-9][0-9]{9}(?![0-9])");
    private static final Pattern CARD =
            Pattern.compile("(?<![0-9])(?:\\d[ -]?){13,19}(?![0-9])");
    private static final Pattern ID =
            Pattern.compile("(?<![0-9])\\d{17}[\\dXx](?![0-9])");

    public PiiGuard(String name) { this.name = name; }

    @Override
    public GuardResult test(String prediction) {
        if (prediction == null) return GuardResult.ok(name);
        Matcher m = EMAIL.matcher(prediction);
        if (m.find()) return GuardResult.fail(name, "email address detected: " + m.group());
        m = PHONE.matcher(prediction);
        if (m.find()) return GuardResult.fail(name, "phone number detected: " + m.group());
        m = CARD.matcher(prediction);
        if (m.find()) return GuardResult.fail(name, "card-like number detected: " + m.group().trim());
        m = ID.matcher(prediction);
        if (m.find()) return GuardResult.fail(name, "id number detected: " + m.group());
        return GuardResult.ok(name);
    }
}
