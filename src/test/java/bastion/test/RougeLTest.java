package bastion.test;

import bastion.metrics.RougeL;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RougeLTest {

    @Test
    void perfect() {
        RougeL r = new RougeL();
        assertEquals(1.0, r.score("the cat sat", "the cat sat"), 1e-9);
    }

    @Test
    void disjoint() {
        RougeL r = new RougeL();
        assertEquals(0.0, r.score("cat dog", "bird fish"), 1e-9);
    }

    @Test
    void partial() {
        RougeL r = new RougeL();
        double s = r.score("the cat sat on the mat", "the cat lay on the mat");
        assertTrue(s > 0.5 && s < 1.0);
    }
}
