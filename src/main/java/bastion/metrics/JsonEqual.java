package bastion.metrics;

import bastion.core.Score;
import bastion.json.Json;

import java.util.List;
import java.util.Map;

/**
 * Structural equality of two JSON documents. Objects are compared
 * order-insensitively; arrays positionally; numbers within a small epsilon.
 * Returns {@code 1.0} when the documents describe the same value.
 */
public final class JsonEqual implements Score {
    @Override
    public double score(String prediction, String reference) {
        if (prediction == null || reference == null) return 0.0;
        try {
            Object a = Json.parse(prediction);
            Object b = Json.parse(reference);
            return equal(a, b) ? 1.0 : 0.0;
        } catch (RuntimeException e) {
            return 0.0;
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean equal(Object a, Object b) {
        if (a == null || b == null) return a == b;
        if (a instanceof Map && b instanceof Map) {
            Map<String, Object> ma = (Map<String, Object>) a;
            Map<String, Object> mb = (Map<String, Object>) b;
            if (ma.size() != mb.size()) return false;
            for (var e : ma.entrySet()) {
                if (!mb.containsKey(e.getKey())) return false;
                if (!equal(e.getValue(), mb.get(e.getKey()))) return false;
            }
            return true;
        }
        if (a instanceof List && b instanceof List) {
            List<Object> la = (List<Object>) a;
            List<Object> lb = (List<Object>) b;
            if (la.size() != lb.size()) return false;
            for (int i = 0; i < la.size(); i++) {
                if (!equal(la.get(i), lb.get(i))) return false;
            }
            return true;
        }
        if (a instanceof Number && b instanceof Number) {
            return Math.abs(((Number) a).doubleValue() - ((Number) b).doubleValue()) < 1e-9;
        }
        return a.equals(b);
    }
}
