package bastion.metrics;

import bastion.core.Score;

import java.util.regex.Pattern;

/**
 * Treats the {@code reference} as a regular expression and reports {@code 1.0}
 * when it matches anywhere inside the prediction. Lets you assert structural
 * properties ("answer ends with a year", "contains an email") without a custom
 * metric.
 */
public final class RegexMatch implements Score {
    @Override
    public double score(String prediction, String reference) {
        if (prediction == null || reference == null) return 0.0;
        try {
            return Pattern.compile(reference).matcher(prediction).find() ? 1.0 : 0.0;
        } catch (RuntimeException e) {
            return 0.0;
        }
    }
}
