package bastion.core;

/**
 * Outcome of a {@link Gate}: an overall verdict plus the per-requirement reasons.
 *
 * @param passed  whether every requirement was satisfied
 * @param reasons map of requirement name to a short explanation
 */
public record GateResult(boolean passed, java.util.Map<String, String> reasons) {}
