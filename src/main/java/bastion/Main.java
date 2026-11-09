package bastion;

import bastion.core.Dataset;
import bastion.core.Example;
import bastion.core.Guard;
import bastion.core.GuardResult;
import bastion.core.Report;
import bastion.core.Runner;
import bastion.core.Score;
import bastion.guards.Guards;
import bastion.metrics.Metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Command-line front-end. Sub-commands:
 * <pre>
 *   bastion metrics                 list built-in metrics
 *   bastion guards                  list built-in guards
 *   bastion evaluate --dataset F [--metrics a,b] [--guards pii,toxicity] [--json]
 *   bastion check --text "..." --guard pii|toxicity
 *   bastion version
 * </pre>
 */
public final class Main {

    public static void main(String[] args) {
        if (args.length == 0) { printHelp(); return; }
        String cmd = args[0];
        Map<String, String> opts = parse(args, 1);

        switch (cmd) {
            case "metrics" -> listMetrics();
            case "guards" -> listGuards();
            case "evaluate" -> evaluate(opts);
            case "check" -> check(opts);
            case "version" -> System.out.println("bastion 1.0.0");
            default -> printHelp();
        }
    }

    private static void listMetrics() {
        System.out.println("Available metrics:");
        for (String name : Metrics.defaults().keySet()) {
            System.out.println("  - " + name);
        }
    }

    private static void listGuards() {
        System.out.println("Available guards:");
        for (String name : Guards.defaults().keySet()) {
            System.out.println("  - " + name);
        }
    }

    private static void evaluate(Map<String, String> opts) {
        String dataset = opts.get("dataset");
        if (dataset == null) { System.err.println("error: --dataset is required"); return; }
        String content;
        try {
            content = Files.readString(Path.of(dataset));
        } catch (IOException e) {
            System.err.println("error: cannot read dataset: " + e.getMessage());
            return;
        }

        List<Example> examples;
        try {
            examples = Dataset.fromJson(content);
        } catch (RuntimeException e) {
            System.err.println("error: failed to parse dataset: " + e.getMessage());
            return;
        }

        Runner runner = new Runner();
        if (opts.containsKey("metrics")) {
            Map<String, Score> all = Metrics.defaults();
            Map<String, Score> chosen = new LinkedHashMap<>();
            for (String n : opts.get("metrics").split(",")) {
                n = n.trim();
                if (all.containsKey(n)) chosen.put(n, all.get(n));
            }
            runner.withMetrics(chosen);
        }
        if (opts.containsKey("guards")) {
            Map<String, Guard> all = Guards.defaults();
            Map<String, Guard> chosen = new LinkedHashMap<>();
            for (String n : opts.get("guards").split(",")) {
                n = n.trim();
                if (all.containsKey(n)) chosen.put(n, all.get(n));
            }
            runner.withGuards(chosen);
        }

        Report report = runner.run(examples);
        if (opts.containsKey("json")) {
            System.out.println(report.toJson());
        } else {
            System.out.println(report.toText());
        }
    }

    private static void check(Map<String, String> opts) {
        String text = opts.get("text");
        String guard = opts.get("guard");
        if (text == null || guard == null) {
            System.err.println("error: --text and --guard are required");
            return;
        }
        Guard g = Guards.defaults().get(guard);
        if (g == null) {
            System.err.println("error: unknown guard '" + guard + "' (known: pii, toxicity)");
            return;
        }
        GuardResult r = g.test(text);
        System.out.println((r.passed() ? "PASS" : "FAIL") + " - " + r.name() + ": " + r.detail());
    }

    private static Map<String, String> parse(String[] args, int from) {
        Map<String, String> opts = new LinkedHashMap<>();
        for (int i = from; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("--") && i + 1 < args.length) {
                opts.put(a.substring(2), args[++i]);
            } else if (a.startsWith("--")) {
                opts.put(a.substring(2), "");
            }
        }
        return opts;
    }

    private static void printHelp() {
        System.out.println("Bastion - zero-dependency LLM evaluation & guardrail toolkit");
        System.out.println("Usage:");
        System.out.println("  bastion metrics");
        System.out.println("  bastion guards");
        System.out.println("  bastion evaluate --dataset FILE [--metrics a,b] [--guards pii,toxicity] [--json]");
        System.out.println("  bastion check --text \"...\" --guard pii|toxicity");
        System.out.println("  bastion version");
    }
}
