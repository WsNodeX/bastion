package bastion.embed;

import java.util.Arrays;
import java.util.List;

/**
 * A hashing-based bag-of-words embedding.
 *
 * <p>Tokens are lower-cased and projected into a fixed number of dimensions; the
 * embedding is then L2-normalised so that {@link Vectors#cosine} becomes a cheap
 * proxy for lexical/semantic overlap. This is intentionally simple and offline:
 * it needs no model download yet gives stable, explainable similarity scores.</p>
 */
public final class HashEmbedding {

    private final int dimensions;

    public HashEmbedding(int dimensions) {
        if (dimensions <= 0) throw new IllegalArgumentException("dimensions must be positive");
        this.dimensions = dimensions;
    }

    public int dimensions() { return dimensions; }

    public double[] encode(String text) {
        double[] vec = new double[dimensions];
        for (String token : tokenize(text)) {
            int dim = Hasher.project(token, dimensions);
            // Sign hashing keeps collisions from all reinforcing the same direction.
            int sign = (Hasher.fnv1a(token) & 1L) == 0 ? 1 : -1;
            vec[dim] += sign;
        }
        return normalize(vec);
    }

    private double[] normalize(double[] vec) {
        double norm = Vectors.l2norm(vec);
        if (norm == 0.0) return vec;
        for (int i = 0; i < vec.length; i++) vec[i] /= norm;
        return vec;
    }

    static List<String> tokenize(String text) {
        return Arrays.asList(text.toLowerCase().split("\\W+"));
    }
}
