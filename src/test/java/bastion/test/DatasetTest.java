package bastion.test;

import bastion.core.Dataset;
import bastion.core.Example;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DatasetTest {

    @Test
    void parsesBareArray() {
        String json = "[{\"id\":\"1\",\"input\":\"q\",\"reference\":\"a\",\"prediction\":\"a\"}]";
        List<Example> ex = Dataset.fromJson(json);
        assertEquals(1, ex.size());
        assertEquals("1", ex.get(0).id());
        assertEquals("a", ex.get(0).prediction());
    }

    @Test
    void parsesWrapped() {
        String json = "{\"examples\":[{\"input\":\"q\",\"prediction\":\"p\"}]}";
        List<Example> ex = Dataset.fromJson(json);
        assertEquals(1, ex.size());
        assertEquals("q", ex.get(0).input());
    }

    @Test
    void parsesMetadata() {
        String json = "[{\"input\":\"q\",\"prediction\":\"p\",\"metadata\":{\"source\":\"gold\"}}]";
        Example e = Dataset.fromJson(json).get(0);
        assertEquals("gold", e.metadata().get("source"));
    }

    @Test
    void roundTrips() {
        List<Example> original = List.of(new Example("1", "q", "a", "a", Map.of("k", "v")));
        String json = Dataset.toJson(original);
        List<Example> back = Dataset.fromJson(json);
        assertEquals(original, back);
    }

    @Test
    void rejectsBadShape() {
        assertThrows(IllegalArgumentException.class, () -> Dataset.fromJson("{\"foo\":1}"));
    }
}
