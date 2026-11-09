package bastion.test;

import bastion.metrics.CosineSimilarity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CosineTest {

    @Test
    void identicalIsOne() {
        CosineSimilarity c = new CosineSimilarity();
        assertEquals(1.0, c.score("hello world foo bar", "hello world foo bar"), 1e-9);
    }

    @Test
    void bounded() {
        CosineSimilarity c = new CosineSimilarity();
        double s = c.score("the weather is sunny today", "it is raining outside now");
        assertTrue(s >= 0.0 && s <= 1.0, "cosine mapped out of range: " + s);
    }

    @Test
    void sameWordsDifferentOrderHigh() {
        CosineSimilarity c = new CosineSimilarity();
        double s = c.score("cat dog bird", "bird dog cat");
        assertTrue(s > 0.9, "bag-of-words reorder should be near 1, got " + s);
    }
}
