package bastion.test;

import bastion.core.Example;
import bastion.core.Report;
import bastion.core.Runner;
import bastion.json.Json;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportTest {

    @Test
    void textReportContainsSummary() {
        Report rep = new Runner().withMetrics("exact_match")
                .run(List.of(new Example("q", "a", "a", "a")));
        String text = rep.toText();
        assertTrue(text.contains("examples: 1"));
        assertTrue(text.contains("metric means"));
    }

    @Test
    void jsonReportIsValid() {
        Report rep = new Runner().withMetrics("exact_match", "f1")
                .run(List.of(new Example("q1", "the cat", "the cat", "the cat"),
                             new Example("q2", "dog", "cat", "fish")));
        String json = rep.toJson();
        Object parsed = Json.parse(json);
        assertTrue(parsed instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) parsed;
        assertEquals(2.0, ((Number) m.get("total")).doubleValue(), 1e-9);
        assertTrue(((Map<?, ?>) m.get("metric_means")).containsKey("f1"));
    }
}
