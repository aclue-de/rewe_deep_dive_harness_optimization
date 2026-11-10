# Exercises

The exercises come from the slides of the deep dive; this repo provides what they need. Each row links to the steps in the [runbook](runbook.md). Before the first exercise: [0. Setup](runbook.md#0-setup).

| Chapter | Slide | Exercise | What it shows | Steps |
|---|---|---|---|---|
| 03 Instruction files | 25 "Exercise (discussion): Slim down an AGENTS.md" | Go through an AGENTS.md line by line: keep it, or move it to a hook, permission, skill or linter rule | Harness fixes can be realized with different tools| example on the slide |
| 05 Hooks | 34 | 1 Ticket context | A `UserPromptSubmit` hook adds context: `REWE-101` in the prompt brings the ticket from `docs/tickets/` along | [1. Ticket context](runbook.md#1-ticket-context-userpromptsubmit-context) |
| 05 Hooks | 34 | 2 Prompt secret guard | A `UserPromptSubmit` hook blocks: a prompt with an API key, IBAN, card number or e-mail list never reaches the model | [2. Prompt secret guard](runbook.md#2-prompt-secret-guard-userpromptsubmit-block) |
| 05 Hooks | 34 | 3 Compact facts | A `SessionStart` hook puts the key project facts back into the context after `/compact` | [3. Compact facts](runbook.md#3-compact-facts-sessionstart-compact) |
| 05 Hooks | 34 | 4 GBOS gate | A `PostToolUse` hook checks AGENTS.md after every edit, reports violations back, and Claude starts the reviewer subagent | [4. GBOS gate](runbook.md#4-gbos-gate-posttooluse) |
| 05 Hooks | 34 | A Harness guard (optional) | A `PreToolUse` hook with `ask`: Claude may not change its own hooks or settings without your OK, and the dialog says what changes | [A. Harness guard](runbook.md#a-optional-harness-guard-pretooluse-ask) |
| 05 Hooks | 34 | B Notify (optional) | A `Notification` hook with `async`: a desktop notice when Claude waits for you | [B. Notify](runbook.md#b-optional-notify-notification-async) |
| 06 Context management | 45 "Exercise: Monitor the prompt cache" | Warm up the cache, switch the model, watch the status line | Switching the model breaks the cache, and that costs tokens | [5. Monitor the prompt cache](runbook.md#5-monitor-the-prompt-cache-slide-45) |
| 06 Context management | 48 "Exercise: A silent test wrapper" | A wrapper: one line when green, the failure when red | Only failures belong in the context, not the whole test log | [6. A silent test wrapper](runbook.md#6-a-silent-test-wrapper-slide-48) |

The optional exercises A and B are for fast groups or for later. Do A last: once it is active, every change to your other hooks asks first.
