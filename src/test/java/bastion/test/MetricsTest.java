package bastion.test;

import bastion.metrics.Contains;
import bastion.metrics.ExactMatch;
import bastion.metrics.F1Score;
import bastion.metrics.JsonEqual;
import bastion.metrics.Levenshtein;
import bastion.metrics.NumericMatch;
import bastion.metrics.RegexMatch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricsTest {

    @Test
    void exactMatch() {
        ExactMatch m = new ExactMatch();
        assertEquals(1.0, m.score("Hello World", "hello world"));
        assertEquals(0.0, m.score("Hello", "Goodbye"));
        assertEquals(0.0, m.score(null, "x"));
    }

    @Test
    void f1() {
        F1Score m = new F1Score();
        assertEquals(1.0, m.score("the cat sat", "the cat sat"), 1e-9);
        assertEquals(0.0, m.score("cat", "dog"), 1e-9);
        double partial = m.score("the cat", "the cat sat");
        assertTrue(partial > 0.5 && partial < 1.0);
    }

    @Test
    void contains() {
        Contains m = new Contains();
        assertEquals(1.0, m.score("abc def ghi", "DEF"));
        assertEquals(0.0, m.score("abc def", "xyz"));
    }

    @Test
    void regexMatch() {
        RegexMatch m = new RegexMatch();
        assertEquals(1.0, m.score("year 2024", "\\d{4}"));
        assertEquals(0.0, m.score("no digits here", "\\d+"));
    }

    @Test
    void numericMatch() {
        NumericMatch m = new NumericMatch();
        assertEquals(1.0, m.score("The answer is 42", "42"));
        assertEquals(0.0, m.score("The answer is 42", "43"));
        assertEquals(0.0, m.score("no number", "5"));
    }

    @Test
    void jsonEqual() {
        JsonEqual m = new JsonEqual();
        assertEquals(1.0, m.score("{\"a\":1,\"b\":2}", "{\"b\":2,\"a\":1}"));
        assertEquals(1.0, m.score("[1,2,3]", "[1,2,3]"));
        assertEquals(0.0, m.score("{\"a\":1}", "{\"a\":2}"));
        assertEquals(0.0, m.score("not json", "{}"));
    }

    @Test
    void levenshtein() {
        Levenshtein m = new Levenshtein();
        assertEquals(1.0, m.score("abc", "abc"), 1e-9);
        assertEquals(0.5, m.score("a", "ab"), 1e-9);
        assertTrue(m.score("kitten", "sitting") > 0.0);
    }
}
