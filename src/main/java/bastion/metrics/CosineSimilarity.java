package bastion.metrics;

import bastion.core.Score;
import bastion.embed.HashEmbedding;
import bastion.embed.Vectors;

/**
 * Lexical cosine similarity: both texts are embedded with a hashing bag-of-words
 * encoder and compared by cosine. A cheap, model-free proxy for "do these two
 * answers talk about the same thing?" that still handles synonym-ish reordering.
 */
public final class CosineSimilarity implements Score {

    private final HashEmbedding embedder;

    public CosineSimilarity() { this(512); }

    public CosineSimilarity(int dimensions) {
        this.embedder = new HashEmbedding(dimensions);
    }

    @Override
    public double score(String prediction, String reference) {
        double sim = Vectors.cosine(embedder.encode(prediction), embedder.encode(reference));
        return (sim + 1.0) / 2.0; // map [-1,1] -> [0,1]
    }
}
