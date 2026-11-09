package bastion.test;

import bastion.metrics.Bleu;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BleuTest {

    @Test
    void perfectMatch() {
        Bleu b = new Bleu();
        assertEquals(1.0, b.score("the cat sat on the mat", "the cat sat on the mat"), 1e-9);
    }

    @Test
    void emptyPrediction() {
        Bleu b = new Bleu();
        assertEquals(0.0, b.score("", "the cat sat"));
    }

    @Test
    void partialCredit() {
        Bleu b = new Bleu();
        double s = b.score("the cat sat", "the cat sat on the mat");
        assertTrue(s > 0.0 && s < 1.0, "expected partial BLEU, got " + s);
    }

    @Test
    void completelyDifferent() {
        Bleu b = new Bleu();
        assertEquals(0.0, b.score("xyz uvw", "the cat sat"), 1e-9);
    }
}
