package bastion.judge;

import bastion.core.Judge;
import bastion.core.Judgement;

import java.util.Locale;

/**
 * An offline {@link Judge} that needs no model backend.
 *
 * <p>It applies a small, explainable rubric: empty or refusal-style answers score
 * low; non-empty, substantive answers that are not merely an echo of the prompt
 * score high. This makes evaluation pipelines fully testable on a plane, and
 * production code can swap in a real model behind the same {@link Judge}
 * interface.</p>
 */
public final class StubJudge implements Judge {

    private static final double PASS = 0.6;

    @Override
    public Judgement judge(String input, String prediction) {
        if (prediction == null || prediction.isBlank()) {
            return new Judgement(false, 0.0, "empty prediction");
        }
        String p = prediction.trim();
        if (isRefusal(p)) {
            return new Judgement(false, 0.15, "looks like a refusal / non-answer");
        }
        if (p.length() < 3) {
            return new Judgement(false, 0.3, "answer too short to be substantive");
        }
        if (input != null && !input.isBlank()
                && p.toLowerCase(Locale.ROOT).equals(input.toLowerCase(Locale.ROOT).trim())) {
            return new Judgement(false, 0.4, "prediction only echoes the prompt");
        }
        return new Judgement(true, 0.85, "stub: non-empty, substantive answer");
    }

    private static boolean isRefusal(String p) {
        String lower = p.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s\\u4e00-\\u9fff]", "")
                .replaceAll("\\s+", " ")
                .trim();
        return lower.equals("i dont know") || lower.equals("i do not know")
                || lower.equals("buzhidao") || lower.equals("wufa huida")
                || lower.startsWith("i cannot") || lower.startsWith("i cant")
                || lower.startsWith("as an ai")
                || lower.contains("i dont know") || lower.contains("i do not know");
    }
}
