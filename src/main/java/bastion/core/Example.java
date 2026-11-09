package bastion.core;

import java.util.Map;

/**
 * A single evaluation record.
 *
 * @param id         stable identifier (defaults to empty when omitted)
 * @param input      the prompt / question fed to the model
 * @param reference  the expected / ground-truth answer (nullable)
 * @param prediction the model output under evaluation (nullable)
 * @param metadata   free-form key/value context (defaults to empty)
 */
public record Example(String id, String input, String reference, String prediction,
                      Map<String, String> metadata) {
    public Example {
        if (id == null) id = "";
        if (input == null) input = "";
        if (reference == null) reference = "";
        if (prediction == null) prediction = "";
        if (metadata == null) metadata = Map.of();
    }

    public Example(String input, String reference, String prediction) {
        this("", input, reference, prediction, Map.of());
    }

    public Example(String id, String input, String reference, String prediction) {
        this(id, input, reference, prediction, Map.of());
    }
}
