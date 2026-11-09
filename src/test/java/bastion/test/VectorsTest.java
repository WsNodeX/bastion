package bastion.test;

import bastion.embed.Vectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VectorsTest {

    @Test
    void dotAndNorm() {
        assertEquals(5.0, Vectors.dot(new double[]{1, 2}, new double[]{1, 2}));
        assertEquals(Math.sqrt(5.0), Vectors.l2norm(new double[]{1, 2}));
    }

    @Test
    void cosineExtremes() {
        assertEquals(1.0, Vectors.cosine(new double[]{1, 0}, new double[]{1, 0}), 1e-9);
        assertEquals(-1.0, Vectors.cosine(new double[]{1, 0}, new double[]{-1, 0}), 1e-9);
        assertEquals(0.0, Vectors.cosine(new double[]{1, 0}, new double[]{0, 1}), 1e-9);
    }

    @Test
    void cosineDegenerate() {
        assertEquals(0.0, Vectors.cosine(new double[]{0, 0}, new double[]{0, 0}));
    }
}
