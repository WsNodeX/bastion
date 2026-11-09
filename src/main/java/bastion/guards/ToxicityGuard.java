package bastion.guards;

import bastion.core.Guard;
import bastion.core.GuardResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Flags disallowed language using a word list. Matching is case-insensitive and
 * bounded by word boundaries so "scun" does not trip on "scunthorpe".
 */
public final class ToxicityGuard implements Guard {

    private final String name;
    private final List<Pattern> patterns = new ArrayList<>();

    public ToxicityGuard(String name) {
        this(name, List.of("badword", "kill yourself", "idiot"));
    }

    public ToxicityGuard(String name, List<String> words) {
        this.name = name;
        for (String w : words) {
            patterns.add(Pattern.compile("\\b" + Pattern.quote(w.toLowerCase(Locale.ROOT)) + "\\b"));
        }
    }

    @Override
    public GuardResult test(String prediction) {
        if (prediction == null) return GuardResult.ok(name);
        String lower = prediction.toLowerCase(Locale.ROOT);
        for (Pattern p : patterns) {
            var m = p.matcher(lower);
            if (m.find()) return GuardResult.fail(name, "flagged phrase detected: " + m.group());
        }
        return GuardResult.ok(name);
    }
}
