package bastion.guards;

import bastion.core.Guard;
import bastion.core.GuardResult;
import bastion.json.Json;

import java.util.List;
import java.util.Map;

/**
 * Asserts that the prediction is a JSON object containing the required keys, and
 * (optionally) that named keys have the expected JSON types.
 *
 * <p>Accepted types: {@code string}, {@code number}, {@code boolean},
 * {@code object}, {@code array}.</p>
 */
public final class JsonFieldGuard implements Guard {

    private final String name;
    private final List<String> requiredKeys;
    private final Map<String, String> types;

    public JsonFieldGuard(String name, List<String> requiredKeys, Map<String, String> types) {
        this.name = name;
        this.requiredKeys = requiredKeys == null ? List.of() : requiredKeys;
        this.types = types == null ? Map.of() : types;
    }

    @Override
    public GuardResult test(String prediction) {
        if (prediction == null) return GuardResult.fail(name, "prediction is null");
        Map<String, Object> obj;
        try {
            obj = Json.parseObject(prediction);
        } catch (RuntimeException e) {
            return GuardResult.fail(name, "not a JSON object: " + e.getMessage());
        }
        for (String key : requiredKeys) {
            if (!obj.containsKey(key)) return GuardResult.fail(name, "missing required key '" + key + "'");
        }
        for (var e : types.entrySet()) {
            if (!obj.containsKey(e.getKey())) {
                return GuardResult.fail(name, "missing key '" + e.getKey() + "' for type check");
            }
            if (!matchType(obj.get(e.getKey()), e.getValue())) {
                return GuardResult.fail(name, "key '" + e.getKey() + "' is not a " + e.getValue());
            }
        }
        return GuardResult.ok(name);
    }

    private static boolean matchType(Object value, String type) {
        return switch (type) {
            case "string" -> value instanceof String;
            case "number" -> value instanceof Number;
            case "boolean" -> value instanceof Boolean;
            case "object" -> value instanceof Map;
            case "array" -> value instanceof List;
            default -> false;
        };
    }
}
