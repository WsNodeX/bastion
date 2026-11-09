package bastion.test;

import bastion.core.Guard;
import bastion.core.GuardResult;
import bastion.guards.CompositeGuard;
import bastion.guards.JsonFieldGuard;
import bastion.guards.LengthBoundGuard;
import bastion.guards.PiiGuard;
import bastion.guards.RegexAllowGuard;
import bastion.guards.RegexDenyGuard;
import bastion.guards.ToxicityGuard;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GuardTest {

    @Test
    void regexAllow() {
        Guard g = new RegexAllowGuard("year", "\\d{4}");
        assertTrue(g.test("founded in 1999").passed());
        assertFalse(g.test("no year here").passed());
    }

    @Test
    void regexDeny() {
        Guard g = new RegexDenyGuard("noSecret", "secret");
        assertTrue(g.test("all good").passed());
        assertFalse(g.test("top secret").passed());
    }

    @Test
    void lengthBound() {
        Guard g = new LengthBoundGuard("len", 5, 10);
        assertTrue(g.test("hello").passed());
        assertFalse(g.test("hi").passed());
        assertFalse(g.test("this string is way too long").passed());
        Guard open = new LengthBoundGuard("min", 3, null);
        assertTrue(open.test("abcd").passed());
    }

    @Test
    void jsonField() {
        Guard g = new JsonFieldGuard("shape",
                List.of("name"),
                Map.of("age", "number"));
        GuardResult ok = g.test("{\"name\":\"x\",\"age\":30}");
        assertTrue(ok.passed());
        assertFalse(g.test("{\"name\":\"x\"}").passed());
        assertFalse(g.test("{\"age\":30}").passed());
        assertFalse(g.test("not json").passed());
    }

    @Test
    void piiDetection() {
        Guard g = new PiiGuard("pii");
        assertFalse(g.test("email me at alice@example.com").passed());
        assertFalse(g.test("call 13800138000 please").passed());
        assertFalse(g.test("card 4111 1111 1111 1111 here").passed());
        assertFalse(g.test("id 11010519491231002X end").passed());
        assertTrue(g.test("no personal info in this sentence").passed());
    }

    @Test
    void toxicity() {
        Guard g = new ToxicityGuard("tox");
        assertFalse(g.test("you are an idiot").passed());
        assertTrue(g.test("you are great").passed());
    }

    @Test
    void compositeAll() {
        Guard g = new CompositeGuard("safe",
                true,
                List.of(new PiiGuard("pii"), new ToxicityGuard("tox")));
        assertTrue(g.test("completely clean text").passed());
        assertFalse(g.test("my email a@b.com").passed());
    }

    @Test
    void compositeAny() {
        Guard g = new CompositeGuard("either",
                false,
                List.of(new RegexAllowGuard("hasDigit", "\\d"),
                        new RegexAllowGuard("hasX", "X")));
        assertTrue(g.test("has 9").passed());
        assertTrue(g.test("has X").passed());
        assertFalse(g.test("nothing").passed());
    }
}
