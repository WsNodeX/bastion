package bastion.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Shared tokenization helper for the lexical metrics. */
final class Tokens {
    private Tokens() {}

    static List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) return List.of();
        return new ArrayList<>(Arrays.asList(text.toLowerCase().split("\\W+")));
    }

    static List<String> ngrams(List<String> tokens, int n) {
        List<String> grams = new ArrayList<>();
        for (int i = 0; i + n <= tokens.size(); i++) {
            StringBuilder b = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) b.append(' ');
                b.append(tokens.get(i + j));
            }
            grams.add(b.toString());
        }
        return grams;
    }
}
