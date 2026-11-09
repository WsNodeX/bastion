package bastion.judge;

import bastion.core.Judge;
import bastion.core.Judgement;

import java.util.function.BiFunction;

/**
 * Adapts an arbitrary {@link BiFunction} into a {@link Judge}. This is the seam
 * where a real LLM (or any remote service) plugs in: supply a function that calls
 * your model and returns a {@link Judgement}.
 */
public final class FunctionJudge implements Judge {

    private final BiFunction<String, String, Judgement> fn;

    public FunctionJudge(BiFunction<String, String, Judgement> fn) {
        this.fn = fn;
    }

    @Override
    public Judgement judge(String input, String prediction) {
        return fn.apply(input, prediction);
    }
}
