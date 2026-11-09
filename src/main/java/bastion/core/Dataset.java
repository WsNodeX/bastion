package bastion.core;

import bastion.json.Json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and serialises evaluation datasets in JSON.
 *
 * <p>Accepted shapes: a bare array of example objects, or an object
 * {@code {"examples": [...]}}. Each example may carry {@code id}, {@code input},
 * {@code reference}, {@code prediction} and {@code metadata}.</p>
 */
public final class Dataset {

    private Dataset() {}

    @SuppressWarnings("unchecked")
    public static List<Example> fromJson(String json) {
        Object root = Json.parse(json);
        List<Map<String, Object>> arr;
        if (root instanceof Map) {
            Object ex = ((Map<String, Object>) root).get("examples");
            if (!(ex instanceof List)) throw new IllegalArgumentException("expected {examples:[...]}");
            arr = (List<Map<String, Object>>) ex;
        } else if (root instanceof List) {
            arr = (List<Map<String, Object>>) root;
        } else {
            throw new IllegalArgumentException("expected JSON array of examples");
        }

        List<Example> out = new ArrayList<>();
        for (Map<String, Object> m : arr) {
            String id = str(m.get("id"));
            String input = str(m.get("input"));
            String reference = str(m.get("reference"));
            String prediction = str(m.get("prediction"));
            Map<String, String> meta = new LinkedHashMap<>();
            Object md = m.get("metadata");
            if (md instanceof Map) {
                for (var e : ((Map<String, Object>) md).entrySet()) {
                    meta.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                }
            }
            out.add(new Example(id, input, reference, prediction, meta));
        }
        return out;
    }

    public static String toJson(List<Example> examples) {
        List<Object> arr = new ArrayList<>();
        for (Example e : examples) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", e.id());
            o.put("input", e.input());
            o.put("reference", e.reference());
            o.put("prediction", e.prediction());
            o.put("metadata", e.metadata());
            arr.add(o);
        }
        return Json.stringify(arr);
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
