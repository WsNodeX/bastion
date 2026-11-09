package bastion.core;

import bastion.json.Json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregated evaluation result. Holds every per-example score plus the mean of
 * each metric across the dataset, and can render itself as human text or JSON.
 */
public final class Report {

    public final List<ScoredExample> items;
    public final Map<String, Double> metricMeans = new LinkedHashMap<>();
    public final int total;

    public Report(List<ScoredExample> items) {
        this.items = List.copyOf(items);
        this.total = items.size();
        if (!items.isEmpty()) {
            for (String name : items.get(0).metricScores().keySet()) {
                double sum = 0.0;
                int count = 0;
                for (ScoredExample se : items) {
                    Double v = se.metricScores().get(name);
                    if (v != null) { sum += v; count++; }
                }
                metricMeans.put(name, count == 0 ? 0.0 : sum / count);
            }
        }
    }

    public double mean(String metric) {
        return metricMeans.getOrDefault(metric, 0.0);
    }

    public String toJson() {
        List<Map<String, Object>> arr = new ArrayList<>();
        for (ScoredExample se : items) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", se.example().id());
            o.put("input", se.example().input());
            o.put("reference", se.example().reference());
            o.put("prediction", se.example().prediction());
            o.put("metrics", se.metricScores());
            Map<String, Object> gr = new LinkedHashMap<>();
            for (var e : se.guardResults().entrySet()) {
                Map<String, Object> g = new LinkedHashMap<>();
                g.put("passed", e.getValue().passed());
                g.put("detail", e.getValue().detail());
                gr.put(e.getKey(), g);
            }
            o.put("guards", gr);
            if (se.judgement() != null) {
                Map<String, Object> j = new LinkedHashMap<>();
                j.put("passed", se.judgement().passed());
                j.put("score", se.judgement().score());
                j.put("rationale", se.judgement().rationale());
                o.put("judgement", j);
            }
            arr.add(o);
        }
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("total", total);
        root.put("metric_means", metricMeans);
        root.put("items", arr);
        return Json.stringify(root);
    }

    public String toText() {
        StringBuilder b = new StringBuilder();
        b.append("Bastion evaluation report\n");
        b.append("=========================\n");
        b.append("examples: ").append(total).append('\n');
        if (!metricMeans.isEmpty()) {
            b.append("metric means:\n");
            for (var e : metricMeans.entrySet()) {
                b.append("  ").append(e.getKey()).append(" = ").append(String.format("%.4f", e.getValue())).append('\n');
            }
        }
        for (ScoredExample se : items) {
            b.append("\n[").append(se.example().id().isEmpty() ? "(no id)" : se.example().id()).append("]\n");
            b.append("  input      : ").append(truncate(se.example().input(), 80)).append('\n');
            b.append("  prediction : ").append(truncate(se.example().prediction(), 80)).append('\n');
            for (var e : se.metricScores().entrySet()) {
                b.append("  metric ").append(e.getKey()).append(" = ").append(String.format("%.4f", e.getValue())).append('\n');
            }
            for (var e : se.guardResults().entrySet()) {
                b.append("  guard ").append(e.getKey()).append(" : ")
                        .append(e.getValue().passed() ? "PASS" : "FAIL")
                        .append(" (").append(e.getValue().detail()).append(")\n");
            }
            if (se.judgement() != null) {
                b.append("  judge      : ").append(se.judgement().passed() ? "PASS" : "FAIL")
                        .append(" score=").append(String.format("%.2f", se.judgement().score()))
                        .append(" - ").append(se.judgement().rationale()).append('\n');
            }
        }
        return b.toString();
    }

    private static String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n) + "...";
    }
}
