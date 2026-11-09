# Exercises

The exercises come from the slides of the deep dive; this repo provides what they need. Each row links to the steps in the [runbook](runbook.md). Before the first exercise: [0. Setup](runbook.md#0-setup).

| Chapter | Slide | Exercise | What it shows | Steps |
|---|---|---|---|---|
| 03 Instruction files | 25 "Exercise (discussion): Slim down an AGENTS.md" | Go through an AGENTS.md line by line: keep it, or move it to a hook, permission, skill or linter rule | Harness fixes can be realized with different tools| example on the slide |
| 05 Hooks | 39 "Hands-on: four exercises" | 1 Protect files | A `PreToolUse` hook stops an edit before it happens: the API key never reaches the code | [1. Protect files](runbook.md#1-protect-files-pretooluse-exit-2) |
| 05 Hooks | 39 | 2 Lint feedback | A `PostToolUse` hook reports problems back, Claude fixes them itself | [2. Lint feedback](runbook.md#2-lint-feedback-posttooluse) |
| 05 Hooks | 39 | 3 Quality gate | A `Stop` hook: Claude may only say "done" when the tests are green | [3. Quality gate](runbook.md#3-quality-gate-stop) |
| 05 Hooks | 39 | 4 Fail closed | Even a broken hook blocks instead of letting everything through | [4. Fail closed](runbook.md#4-fail-closed-pretooluse-json) |
| 06 Context management | 45 "Exercise: Monitor the prompt cache" | Warm up the cache, switch the model, watch the status line | Switching the model breaks the cache, and that costs tokens | [5. Monitor the prompt cache](runbook.md#5-monitor-the-prompt-cache-slide-45) |
| 06 Context management | 48 "Exercise: A silent test wrapper" | A wrapper: one line when green, the failure when red | Only failures belong in the context, not the whole test log | [6. A silent test wrapper](runbook.md#6-a-silent-test-wrapper-slide-48) |
| 06 Context management | 49 "Exercise: Enforce it with hooks" | A `PreToolUse` hook puts the wrapper in front of the test command | A hook turns the request into a rule: Claude no longer has to remember the wrapper | [7. Enforce it with a hook](runbook.md#7-enforce-it-with-a-hook-slide-49) |

**Bonus** (not on a slide; fits chapter 02 Verification & Observability)

| Exercise | What it shows | Steps |
|---|---|---|
| Log every hook call to `.harness/logs.jsonl` | Which data Claude Code sends to hooks, and when: the input your other hooks work with | [8. Bonus: log every hook call](runbook.md#8-bonus-log-every-hook-call) |
