<div align="center">

# Bastion

### A zero-dependency Java toolkit for evaluating LLM outputs and enforcing output guardrails

[![Java](https://img.shields.io/badge/Java-21-blue)](https://www.java.com/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Dependencies](https://img.shields.io/badge/dependencies-0%20(zero)-brightgreen)](src/main/java)
[![Platform](https://img.shields.io/badge/platform-JVM%20%2F%20any-brightgreen)](src/main/java)
[![Tests](https://img.shields.io/badge/tests-57%20passing-brightgreen)](src/test/java)
[![CI](https://img.shields.io/badge/CI-GitHub%20Actions-blue)](.github/workflows/ci.yml)

*Score answers, block unsafe output, and turn quality into a binary release decision — all offline, on the standard library.*

</div>

---

<p align="center">
  <a href="#-why-bastion">Why</a> ·
  <a href="#-features">Features</a> ·
  <a href="#-installation">Install</a> ·
  <a href="#-quick-start">Quick Start</a> ·
  <a href="#-architecture">Architecture</a> ·
  <a href="#-api-reference">API</a> ·
  <a href="#-roadmap">Roadmap</a> ·
  <a href="#-contributing">Contributing</a> ·
  <a href="#-license">License</a>
</p>

---

## ✨ Why Bastion

Shipping an LLM feature is easy. *Knowing it is good* is hard.

Most teams ship a prompt, eyeball a few outputs, and call it done — until a
customer receives a confidently wrong answer, or a model leaks a phone number
that was never supposed to leave the building. Quality becomes a vibe instead of
a number.

Bastion treats evaluation as **engineering**, not folklore:

- **Deterministic metrics** (exact match, F1, BLEU, ROUGE-L, cosine, …) score
  answers the same way every run, on your laptop, with no API key.
- **Guardrails** screen outputs for PII, toxicity, length bounds and JSON shapes
  before they ever reach a user.
- A **judge** seam lets you plug a real model in for "is this actually helpful?"
  while the bundled offline judge keeps tests hermetic.
- A **gate** collapses every score and guard into one `PASS` / `FAIL` verdict,
  the kind you can gate a deploy on.

It is one JAR's worth of standard-library Java. No Spring, no PyTorch, no
network. That is the whole point: evaluation should be the most boring, most
reliable part of your stack.

## 🚀 Features

- **10 built-in metrics**, each a pure function of `(prediction, reference)`:
  `exact_match`, `f1`, `bleu`, `rouge_l`, `cosine`, `levenshtein`, `contains`,
  `numeric_match`, `json_equal`, `regex_match`.
- **Offline semantic similarity** via a hashing bag-of-words encoder — no
  embedding model download required.
- **Guardrails**: regex allow/deny, length bounds, JSON-field/type checks, PII
  heuristics (email / mobile / card / ID), toxicity wordlist, and composite
  `allOf` / `anyOf` policies.
- **LLM-as-judge** abstraction with a deterministic `StubJudge` for tests and a
  `FunctionJudge` adapter for your real model.
- **Datasets** load from JSON (`[{"input","reference","prediction",...}]`) and
  reports render to both human text and machine JSON.
- **CLI** for one-shot evaluation and single-output screening.
- **Zero runtime dependencies.** JDK 21 standard library only.

## 📦 Installation

Bastion is plain Java 21 source — drop it into your build.

**From source**

```bash
git clone https://github.com/WsNodeX/bastion.git
cd bastion
# compile (no external dependencies needed)
javac -d build/main $(find src/main/java -name '*.java')
javac -cp "build/main" -d build/main src/main/java/bastion/examples/Demo.java
java -cp build/main bastion.examples.Demo
```

**As a library**

Copy `src/main/java/bastion` into your module, or publish the compiled classes
to your internal repository. There is nothing else to add to your classpath.

**Requirements:** JDK 21 or newer.

## ⚡ Quick Start

Score a small dataset and gate the result:

```java
import bastion.core.*;
import bastion.guards.Guards;
import bastion.judge.StubJudge;
import java.util.*;

public class QuickStart {
    public static void main(String[] args) {
        List<Example> data = List.of(
            new Example("q1", "What is 2+2?", "4", "The answer is 4."),
            new Example("q2", "Capital of France?", "Paris", "I don't know."));

        Report report = new Runner()
            .withMetrics("exact_match", "f1", "rouge_l")
            .withGuards(Map.of("pii", Guards.defaults().get("pii")))
            .withJudge(new StubJudge())
            .run(data);

        System.out.println(report.toText());

        Gate gate = new Gate().requireMetric("f1", 0.5).requireGuard("pii");
        for (ScoredExample se : report.items) {
            GateResult g = gate.evaluate(se);
            System.out.println(se.example().id() + " -> " + (g.passed() ? "SHIP" : "HOLD"));
        }
    }
}
```

**Expected output (abridged):**

```
Bastion evaluation report
=========================
examples: 2
metric means:
  exact_match = 0.0000
  f1 = 0.1667
  rouge_l = 0.1667

[q1]
  prediction : The answer is 4.
  metric exact_match = 0.0000
  metric f1 = 0.5000
  metric rouge_l = 0.5000
  guard pii : PASS (passed)
  judge      : PASS score=0.85 - stub: non-empty, substantive answer
[q2]
  prediction : I don't know.
  judge      : FAIL score=0.15 - looks like a refusal / non-answer
q1 -> SHIP
q2 -> HOLD
```

**CLI**

```bash
# list available metrics / guards
java -cp build/main bastion.Main metrics
java -cp build/main bastion.Main guards

# evaluate a dataset file (JSON array of examples)
java -cp build/main bastion.Main evaluate --dataset answers.json --metrics f1,rouge_l --json

# screen a single string against a guard
java -cp build/main bastion.Main check --text "email me at a@b.com" --guard pii
# -> FAIL - pii: email address detected: a@b.com
```

## 🏗️ Architecture

Bastion is a thin, layered pipeline. Each layer is independent and unit-tested.

```
          ┌───────────────────────────  bastion.Main (CLI)  ───────────────────────────┐
          │                                                                             │
          │   Dataset (JSON) ──▶ Runner ──┬──▶ metrics/*  (Score: prediction × reference)│
          │                              ├──▶ guards/*   (Guard:  prediction → PASS/FAIL) │
          │                              └──▶ judge/*    (Judge:  input × prediction)    │
          │                                     │                                       │
          │                                     ▼                                       │
          │                              ScoredExample                                  │
          │                                     │                                       │
          │                                     ▼                                       │
          │                       Report  ◀── Gate  (thresholds + required guards)       │
          └─────────────────────────────────────────────────────────────────────────────┘

  core/      Example · ScoredExample · Score · Guard · GuardResult · Judge · Judgement
             Runner · Report · Gate · GateResult · Dataset
  json/      Json            (zero-dependency reader/writer)
  embed/     Hasher · HashEmbedding · Vectors      (hashing bag-of-words encoder)
  metrics/   ExactMatch · F1Score · Bleu · RougeL · CosineSimilarity · Levenshtein
             Contains · NumericMatch · JsonEqual · RegexMatch · Metrics (registry)
  guards/    RegexAllow/Deny · LengthBound · JsonField · Pii · Toxicity · Composite
  judge/     StubJudge (offline) · FunctionJudge (pluggable model)
```

The only shared state is the immutable `Example`; everything else is a pure
function, which is why the whole suite runs offline in milliseconds.

## 🔌 API Reference

**Core types**

| Type | Purpose |
|------|---------|
| `Example` | A record: `id, input, reference, prediction, metadata` |
| `ScoredExample` | An `Example` plus metric scores, guard results and a judgement |
| `Report` | Aggregated results: per-example scores + `metricMeans` + `toText()`/`toJson()` |
| `Runner` | Composes metrics, guards and a judge, then `run(List<Example>)` |
| `Gate` | `requireMetric(name, min)` / `requireGuard(name)` → `evaluate(ScoredExample)` |
| `Score` | `double score(String prediction, String reference)` — a metric |
| `Guard` | `GuardResult test(String prediction)` — an output gate |
| `Judge` | `Judgement judge(String input, String prediction)` — LLM-as-judge seam |

**Metrics (`Metrics.defaults()`)**

| Name | Range | Meaning |
|------|-------|---------|
| `exact_match` | 0–1 | Normalized string equality |
| `f1` | 0–1 | Token-level harmonic mean of precision/recall |
| `bleu` | 0–1 | N-gram precision (≤4) with brevity penalty |
| `rouge_l` | 0–1 | Longest-common-subsequence F1 |
| `cosine` | 0–1 | Hashing bag-of-words cosine, mapped from [−1,1] |
| `levenshtein` | 0–1 | `1 − editDistance / maxLen` |
| `contains` | 0–1 | Reference substring present in prediction |
| `numeric_match` | 0–1 | First number within tolerance |
| `json_equal` | 0–1 | Structural JSON equality (order-insensitive objects) |
| `regex_match` | 0–1 | Reference is a regex matched by prediction |

**Guards (`Guards`)**

| Factory | Behaviour |
|---------|-----------|
| `regexAllow(name, pat)` | Pass only if prediction matches `pat` |
| `regexDeny(name, pat)` | Fail if prediction matches `pat` |
| `lengthBound(name, min, max)` | Enforce inclusive character length |
| `jsonField(name, keys, types)` | Require JSON object keys / value types |
| `pii(name)` | Block email / mobile / card / ID patterns |
| `toxicity(name, words)` | Block a wordlist (word-boundary, case-insensitive) |
| `allOf` / `anyOf` | Compose guards with boolean logic |

## 🧭 Roadmap

- [ ] `evaluate` CLI flag to emit a JUnit-style XML report for CI gates
- [ ] Weighted composite metrics and per-metric pass thresholds in `Report`
- [ ] `exact_set_match` metric for multi-answer questions
- [ ] Optional streaming judge client (OpenAI-compatible chat completions)
- [ ] Built-in dataset loaders for common benchmarks (MMLU, GSM8K-shaped)
- [ ] Maven Central publishing with a packaged `bastion.jar`

## 🤝 Contributing

Contributions are welcome. Because Bastion is dependency-free by design, please
keep new code on the standard library and add a unit test for every behaviour.

```bash
# run the suite locally
javac -d build/main $(find src/main/java -name '*.java')
javac -cp "build/main:junit.jar" -d build/test $(find src/test/java -name '*.java')
java -cp "build/main:build/test:junit.jar" \
  org.junit.platform.console.ConsoleLauncher --scan-class-path build/test
```

Open an issue to discuss direction before large changes.

## 📄 License

Released under the [MIT License](LICENSE).

---

<hr/>

# 用 Java 给大模型答案“打分”：一个零依赖的评测与护栏工具箱

> 下面这篇长文既是 Bastion 的使用说明，也是一篇可以直接发布到掘金 / 知乎 /
> 小红书 / B 站的科普文底稿。它从“为什么需要评测”讲到“核心原理”再到
> “关键实现点”，读者看完能直接动手。

## 一、它解决什么问题

做 AI 应用的人都有一个共同痛点：**模型跑起来了，但好不好的标准说不清**。

- 产品经理想知道“这一版 prompt 比上一版强多少”，只能靠肉眼比对；
- 上线前没人敢保证模型不会输出手机号、身份证号；
- “拒答”“胡说”这些坏答案混在好答案里，靠人盯根本盯不过来。

Bastion 的思路是：把“答案质量”和“输出安全”变成**可量化、可重复、可拦截**
的工程信号。它不依赖任何第三方库，纯 JDK 标准库就能离线跑完一整套评测。

## 二、核心原理

整个工具是一条四段流水线：

1. **指标（Metric）**：把“预测答案”和“标准答案”喂给一组纯函数，输出 0–1
   的分。比如 `exact_match` 看是否完全一致，`f1` 看词级别的重合，`rouge_l`
   看最长公共子序列，`bleu` 看 n-gram 精度加长度惩罚。
2. **护栏（Guard）**：只针对“模型产出的文本”做判定——有没有邮箱？长度超没超？
   是不是合法 JSON、字段齐不齐？任何一条不过就拦下。
3. **裁判（Judge）**：回答“这答案到底有没有用”。内置一个**离线裁判**，不需要
   联网、不需要 API Key，保证单测永远可跑；真要更准，把你的模型接进
   `FunctionJudge` 即可。
4. **门禁（Gate）**：把上面所有分数和护栏收拢成一个 `PASS / FAIL`。这就是你
   能在 CI 里拿来卡发布的那一关。

关键是：**每一环都是纯函数、输入不可变**。所以整条流水线可离线、可复现、
可在毫秒级跑完成百上千条样本。

## 三、几个面试常问的实现点

- **BLEU 为什么不简单用 n-gram 命中数？**
  直接数会高估重复词。Bastion 对预测里的每个 n-gram 做“裁剪计数”
  （clipped count，取预测出现次数与参考答案出现次数的较小值），再除以总
  预测 n-gram 数，最后对 1–4 阶精度取几何平均，并乘长度惩罚。这样预测把
  一个词复制十遍也不会得满分。
- **没有向量模型怎么算语义相似度？**
  用“哈希词袋”：把每个词哈希到固定维度、带符号累加、再做 L2 归一化，两个
  向量的余弦就是廉价却稳定的一致性代理。它不做语义理解，但足以捕捉“换种
  说法、顺序打乱”的近似重复。
- **PII 检测怎么做才不误伤？**
  用保守正则：邮箱、中国大陆手机号（1[3-9] 开头 11 位）、类卡号数字串、18
  位身份证。原则是“宁可错杀”，而且它只是筛查层，不是合规级 DLP。
- **裁判为什么要“离线桩”？**
  因为单测不能依赖网络。离线桩按“空答案/拒答/只是复述问题/有实质内容”给
  出可解释的 0–1 分，真实模型只是同一个接口的另一种实现。

## 四、和“随手写断言”的对比

| 方案 | 可复现 | 离线 | 多指标 | 可拦截 | 维护成本 |
|------|--------|------|--------|--------|----------|
| `assert answer == "4"` | 低 | 是 | 否 | 否 | 低但脆弱 |
| 调大模型当评委肉眼看 | 低 | 否 | 视人而定 | 否 | 高 |
| 接重型评测框架 | 高 | 否（常需联网） | 是 | 部分 | 高 |
| **Bastion** | **高** | **是** | **是（10 种）** | **是（Gate）** | **低（零依赖）** |

结论：Bastion 不是要取代评测平台，而是把“评测”这件本该最无聊、最可靠的事，
做成你项目里一个稳定、零负担的标准件。

## 五、如何扩展成一个发布稿

- **选题角度**：可写“校招生如何用 Java 手搓一个 LLM 评测工具”“零依赖实现
  BLEU/ROUGE 的坑”“大模型上线前的护栏三道防线”。
- **配图建议**：把上面的架构 ASCII 图换成手绘风流程图； BLEU 裁剪计数用
  一个小表格演示，读者一眼能懂。
- **互动点**：文末放一个“你最关心哪类评测指标？”的投票，引流到仓库。
- **代码佐证**：直接 `git clone` 跑 `bastion.examples.Demo`，把输出截图贴进去，
  可信度拉满。

> 本文所有内容均以原创实现呈现；把它扩写成发布稿时，建议保留“原理 + 可运行
> 代码 + 对比表”的三段式结构，既好读又利于搜索收录。
