# Runbook

Step by step: what to do, and how you know it worked. Run all commands from the repo root.

**Test it alone** runs your hook the way Claude Code does, without Claude:

1. Claude Code starts the hook and writes a JSON to its stdin that says what is happening, e.g. "Claude wants to put `String apiKey = "demo-supplier-key-…";` into `Client.java`".
2. `echo '{…}'` prints exactly that JSON.
3. `|` feeds it to the hook.
4. `java .claude/hooks/<YourHook>.java` starts the hook; replace `<YourHook>` with your file name.

The command is the same for everyone: Claude Code defines the JSON, and the expected exit code and JSON fields. Only the wording of your messages may differ.

**Live** is the same check in a real Claude session: you give Claude a task that triggers your hook and watch what happens.

Commands work in Git Bash and PowerShell. The exit code is `echo $?` in Git Bash and `$LASTEXITCODE` in PowerShell.

## 0. Setup

| Do | It worked if |
|---|---|
| `java -version` | it shows `25` |
| `java backend/InventoryTest.java` | about 250 `[debug]` lines, six `ok` lines, then `OK` |
| `java backend/Server.java` | http://localhost:8000 shows the item list (keep the terminal open, stop with Ctrl+C) |
| `claude` in a second terminal | Claude Code starts in this folder |

## 1. Protect files (`PreToolUse`, exit 2)

Build a hook that blocks edits that write an API key into code.

**Test it alone.** With a key, the hook must block:

```bash
echo '{"hook_event_name":"PreToolUse","tool_name":"Edit","tool_input":{"file_path":"Client.java","new_string":"String apiKey = \"demo-supplier-key-0123456789\";"}}' | java .claude/hooks/<YourHook>.java
```

It worked if the exit code is `2` and stderr says why.

Without a key, the hook must let the edit through:

```bash
echo '{"hook_event_name":"PreToolUse","tool_name":"Edit","tool_input":{"file_path":"Client.java","new_string":"String apiKey = System.getenv(\"API_KEY\");"}}' | java .claude/hooks/<YourHook>.java
```

It worked if the exit code is `0` and there is no output.

**Live.**
1. Enter the hook in `.claude/settings.json` (see README), so Claude Code runs it before every edit.
2. Restart `claude`.
3. Ask Claude: *"Move the constant SUPPLIER_API_KEY from backend/Server.java into a new class backend/Config.java."* The new file would contain the hard-coded key, so your hook must stop it.

It worked if:
- Claude reports that the edit was blocked and names the key,
- the hard-coded key does not end up in any new file. Claude often retries with `System.getenv(...)`: that is the hook doing its job.

## 2. Lint feedback (`PostToolUse`)

Build a hook that runs `CheckAgentsMd.java` after an edit of `AGENTS.md` and feeds the findings back to Claude.

**Test it alone.**

```bash
echo '{"hook_event_name":"PostToolUse","tool_name":"Edit","tool_input":{"file_path":"AGENTS.md"}}' | java .claude/hooks/<YourHook>.java
```

It worked if the exit code is `2` and stderr lists the `VIOLATION` lines. With `"file_path":"backend/Server.java"` the exit code must be `0`.

**Live.** Ask Claude: *"Add a rule about stock bookings to AGENTS.md."*

It worked if:
- Claude shows the findings after the edit,
- if your hook message names the subagent `gbos-reviewer`, Claude offers it or starts it.

PostToolUse runs **after** the edit, so the change is already in `AGENTS.md`. The hook cannot stop or undo it, it can only report problems; Claude fixes them with a second edit. Blocking before the edit is PreToolUse (exercise 1).

## 3. Quality gate (`Stop`)

Build a hook that lets Claude finish only when `java backend/InventoryTest.java` is green. When the tests are red, give Claude only the `FAIL` lines, not the ~250 debug lines.

**Test it alone.** With green tests:

```bash
echo '{"hook_event_name":"Stop","stop_hook_active":false}' | java .claude/hooks/<YourHook>.java
```

It worked if the exit code is `0` and there is no output. With `"stop_hook_active":true` it must also let Claude stop, which is the loop guard.

**Live.** Ask Claude: *"Allow negative stock for backorders. Don't touch the tests."*

It worked if:
- Claude cannot finish and names the failing test `stock never negative`,
- Claude then fixes the code, or explains the conflict and asks you,
- `git diff` shows only the code change, the tests are untouched.

## 4. Fail closed (`PreToolUse`, JSON)

Rebuild the hook from exercise 1:
- answer with JSON `"permissionDecision": "deny"` and exit `0`,
- add `"onFailure": "block"` to it in `settings.json`.

**Test it alone.** Use the first command from exercise 1. It worked if the exit code is `0` and stdout contains `"permissionDecision": "deny"`.

**Live.** What happens when the hook itself is broken? Normally Claude Code ignores a failing hook and lets the edit through (fail open). With `"onFailure": "block"` it blocks instead (fail closed).

1. Break the hook: rename it, e.g. to `<YourHook>.java.off`. Now `java` cannot start it.
2. Ask Claude: *"Add a comment to backend/Server.java."*
   It worked if the edit is blocked with `failed; blocking because onFailure is "block"`.
3. For comparison: remove the `onFailure` line, restart `claude`, ask again.
   Now the edit goes through although the hook is broken. Undo it with `git checkout backend/Server.java`.
4. Clean up: rename the hook back and put the `onFailure` line back.

## 5. Monitor the prompt cache (slide 45)

Needs Claude Code 2.1.260 or later (`claude --version`).

| Do | It worked if |
|---|---|
| `claude --settings tools/status-line/settings.json` | the status line shows `cache: waiting for the first answer` |
| Ask two short questions | `read` grows, `hit` goes up, `misses` stays `0` |
| `/model`, pick another model, ask once more | `hit` drops, `write` jumps, `misses` goes up: the new model cannot use the old cache |

No status line under the input box? Then Claude Code is older than 2.1.260 or cannot start `java`. The same numbers are also in `/usage`: run it after each step and look at the line `Prompt cache (main)`.

## 6. A silent test wrapper (slide 48)

Write `bin/RunSilent.java`: `java bin/RunSilent.java <label> <command...>` runs the command, prints `✓ <label>` when it passes, and `✗ <label>` plus the last 60 lines when it fails.

**Test it alone.**

```bash
java bin/RunSilent.java tests java backend/InventoryTest.java
```

It worked if you see one line `✓ tests` and exit code `0`, instead of about 250 lines.

For the red case, change `item.quantity + delta < 0` in `backend/Inventory.java` to `false`, run again, then undo the change. It worked if you see `✗ tests`, the line `FAIL  stock never negative` and exit code `1`.

**Live.**
1. Ask Claude: *"Run java backend/InventoryTest.java."* Claude gets all ~250 lines: it does not know your wrapper.
2. Ask Claude: *"Run the tests with java bin/RunSilent.java tests java backend/InventoryTest.java."* It worked if the tool result is a single line `✓ tests`.

To make Claude use the wrapper without being told, slide 48 adds a line to `CLAUDE.md`. That is only a request; exercise 7 enforces it with a hook.

## 7. Enforce it with a hook (slide 49)

Write a `PreToolUse` hook with matcher `Bash`. When the command is `java backend/InventoryTest.java`, reply with `updatedInput`: the whole `tool_input`, with the command replaced by `java bin/RunSilent.java tests java backend/InventoryTest.java`. Any other command: no output.

**Test it alone.**

```bash
echo '{"hook_event_name":"PreToolUse","tool_name":"Bash","tool_input":{"command":"java backend/InventoryTest.java","description":"Run the tests"}}' | java .claude/hooks/<YourHook>.java
```

It worked if:
- stdout contains `"updatedInput"` with the wrapped command and still `"description": "Run the tests"`,
- with `"command":"git status"` there is no output.

**Live.** Ask Claude again, without mentioning the wrapper: *"Run java backend/InventoryTest.java."* It worked if Claude runs exactly that command, but the result is only `✓ tests`: the hook put the wrapper in front.

Name the command in the prompt. With just *"Run the tests"* Claude may follow the messy AGENTS.md and try `mvn test`.

## 8. Bonus: log every hook call

Write a hook that appends the JSON from stdin unchanged as one line to `.harness/logs.jsonl` and exits `0`. Register it for `PreToolUse`, `PostToolUse` and `Stop`, without matcher, with `"async": true` so it does not slow Claude down. `.harness/` is in `.gitignore`.

One JSON object per line (JSON Lines) instead of one `logs.json`: a line can simply be appended, a JSON array would have to be read and rewritten on every call.

**Test it alone.**

```bash
echo '{"hook_event_name":"Stop","stop_hook_active":false}' | java .claude/hooks/<YourHook>.java
```

It worked if the exit code is `0`, there is no output, and `.harness/logs.jsonl` has one more line.

**Live.** Restart `claude` and give Claude a small task, e.g. *"Run java backend/InventoryTest.java."*

It worked if `.harness/logs.jsonl` shows one line per hook call: `PreToolUse` with `tool_input`, `PostToolUse` with `tool_response`, `Stop` with `stop_hook_active`. That is exactly the JSON your other hooks receive.
