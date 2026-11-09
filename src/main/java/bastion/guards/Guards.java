package bastion.guards;

import bastion.core.Guard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory and default registry for the built-in output guardrails.
 */
public final class Guards {

    private Guards() {}

    public static Map<String, Guard> defaults() {
        Map<String, Guard> m = new LinkedHashMap<>();
        m.put("pii", new PiiGuard("pii"));
        m.put("toxicity", new ToxicityGuard("toxicity"));
        return m;
    }

    public static Guard regexAllow(String name, String pattern) {
        return new RegexAllowGuard(name, pattern);
    }

    public static Guard regexDeny(String name, String pattern) {
        return new RegexDenyGuard(name, pattern);
    }

    public static Guard lengthBound(String name, Integer min, Integer max) {
        return new LengthBoundGuard(name, min, max);
    }

    public static Guard jsonField(String name, List<String> requiredKeys, Map<String, String> types) {
        return new JsonFieldGuard(name, requiredKeys, types);
    }

    public static Guard pii(String name) { return new PiiGuard(name); }

    public static Guard toxicity(String name, List<String> words) { return new ToxicityGuard(name, words); }

    public static Guard allOf(String name, List<Guard> guards) {
        return new CompositeGuard(name, true, guards);
    }

    public static Guard anyOf(String name, List<Guard> guards) {
        return new CompositeGuard(name, false, guards);
    }
}
