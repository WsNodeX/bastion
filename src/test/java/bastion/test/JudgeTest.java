package bastion.test;

import bastion.core.Judge;
import bastion.core.Judgement;
import bastion.judge.FunctionJudge;
import bastion.judge.StubJudge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JudgeTest {

    @Test
    void stubEmptyIsFail() {
        Judgement j = new StubJudge().judge("q", "   ");
        assertFalse(j.passed());
        assertEquals(0.0, j.score(), 1e-9);
    }

    @Test
    void stubRefusalIsFail() {
        Judgement j = new StubJudge().judge("q", "I don't know");
        assertFalse(j.passed());
    }

    @Test
    void stubGoodAnswer() {
        Judgement j = new StubJudge().judge("What is 2+2?", "The sum of 2 and 2 is 4.");
        assertTrue(j.passed());
        assertEquals(0.85, j.score(), 1e-9);
    }

    @Test
    void functionJudgePlugsIn() {
        Judge j = new FunctionJudge((input, pred) ->
                new Judgement(pred.contains("Paris"), 1.0, "contains Paris"));
        assertTrue(j.judge("cap?", "Paris is the capital").passed());
        assertFalse(j.judge("cap?", "Berlin").passed());
    }
}
