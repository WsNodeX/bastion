package bastion.test;

import bastion.Main;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CliTest {

    private String capture(Runnable r) {
        PrintStream old = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buf, true, StandardCharsets.UTF_8));
        try {
            r.run();
        } finally {
            System.setOut(old);
        }
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void metricsListsBuiltins() {
        String out = capture(() -> Main.main(new String[]{"metrics"}));
        assertTrue(out.contains("exact_match"));
        assertTrue(out.contains("rouge_l"));
    }

    @Test
    void guardsListsBuiltins() {
        String out = capture(() -> Main.main(new String[]{"guards"}));
        assertTrue(out.contains("pii"));
        assertTrue(out.contains("toxicity"));
    }

    @Test
    void checkCommand() {
        String out = capture(() -> Main.main(new String[]{"check", "--text", "email a@b.com", "--guard", "pii"}));
        assertTrue(out.contains("FAIL"));
    }

    @Test
    void evaluateCommand() throws Exception {
        Path tmp = Files.createTempFile("ds", ".json");
        Files.writeString(tmp, "[{\"id\":\"1\",\"input\":\"q\",\"reference\":\"the cat sat\",\"prediction\":\"the cat sat\"}]");
        String out = capture(() -> Main.main(new String[]{"evaluate", "--dataset", tmp.toString(), "--metrics", "exact_match,f1"}));
        assertTrue(out.contains("examples: 1"));
        assertTrue(out.contains("exact_match = 1.0000"));
        Files.deleteIfExists(tmp);
    }

    @Test
    void version() {
        String out = capture(() -> Main.main(new String[]{"version"}));
        assertTrue(out.contains("bastion"));
    }
}
