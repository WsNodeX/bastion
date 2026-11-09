package bastion.test;

import bastion.json.Json;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonTest {

    @Test
    void parsesPrimitives() {
        assertEquals(1.0, ((Number) Json.parse("1")).doubleValue());
        assertEquals(-2.5, ((Number) Json.parse("-2.5")).doubleValue());
        assertTrue((Boolean) Json.parse("true"));
        assertNull(Json.parse("null"));
        assertEquals("hi", Json.parse("\"hi\""));
    }

    @Test
    void parsesNested() {
        Map<?, ?> m = Json.parseObject("{\"a\":[1,2,{\"b\":\"x\"}],\"c\":null}");
        assertEquals(2, m.size());
        @SuppressWarnings("unchecked")
        List<Object> a = (List<Object>) m.get("a");
        assertEquals(3, a.size());
        @SuppressWarnings("unchecked")
        Map<String, Object> inner = (Map<String, Object>) a.get(2);
        assertEquals("x", inner.get("b"));
        assertNull(m.get("c"));
    }

    @Test
    void roundTrips() {
        String src = "{\"name\":\"J\\\"\",\"n\":3,\"ok\":true,\"list\":[1,\"two\",false],\"obj\":{\"k\":null}}";
        Object parsed = Json.parse(src);
        String back = Json.stringify(parsed);
        assertEquals(parsed, Json.parse(back));
    }

    @Test
    void rejectsBadInput() {
        assertThrows(RuntimeException.class, () -> Json.parse("{bad"));
        assertThrows(RuntimeException.class, () -> Json.parse("{\"a\":1"));
        assertThrows(RuntimeException.class, () -> Json.parse("trux"));
    }
}
