package bastion.test;

import bastion.embed.HashEmbedding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashEmbeddingTest {

    @Test
    void deterministic() {
        HashEmbedding e = new HashEmbedding(256);
        double[] a = e.encode("the quick brown fox");
        double[] b = e.encode("the quick brown fox");
        assertArrayEquals(a, b, 1e-12);
    }

    @Test
    void respectsDimensions() {
        HashEmbedding e = new HashEmbedding(128);
        assertEquals(128, e.encode("anything").length);
    }

    @Test
    void normalized() {
        double[] v = new HashEmbedding(512).encode("a b c d e");
        double norm = 0;
        for (double x : v) norm += x * x;
        assertEquals(1.0, Math.sqrt(norm), 1e-9);
    }
}
