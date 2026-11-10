# Deep Dive Harness Optimization – Exercise Repo

Small warehouse app in Java with a GBOS-style AGENTS.md, a hard-coded API key and two sample tickets. You build the hooks yourself.

**Requirements:** JDK 25, git and Claude Code. No build tool: to keep the lab simple, every program runs straight from source, e.g. `java backend/Server.java` (JDK 22+).

## Check Java first

```bash
java -version          # must show 25
```

Shows 21 or older? Then `java backend/Server.java` fails with `cannot find symbol … class Inventory` (German JDK: `Symbol nicht gefunden`). Older JDKs compile only the one file you start, not its neighbours. Install JDK 25:

| OS | Install | Which `java` comes first? |
|---|---|---|
| Windows | `winget install EclipseAdoptium.Temurin.25.JDK` | `where.exe java` |
| macOS | `brew install --cask temurin@25` | `which java` |
| Linux | `sdk install java 25-tem` (SDKMAN) or your package manager | `which java` |

Then **close the whole program your terminal runs in** (VS Code with all its windows, Windows Terminal, IntelliJ, …) and start it again: a new tab keeps the old `PATH`. Check `java -version` again. Still the old version? An older JDK is earlier on the `PATH`: move JDK 25 to the front or remove the old one.

## Start

```bash
java backend/Server.java                                  # app on http://localhost:8000
claude                                                    # in a second terminal, in this folder
```

Run both from the repo root: the server looks for `frontend/` relative to the current folder.

## Structure

```
backend/            Server.java (API + frontend), Inventory.java, InventoryTest.java (chatty on purpose)
frontend/           static page that calls the API
tools/status-line/  status line showing the prompt cache (slide 45)
AGENTS.md           GBOS-style instructions for coding agents
docs/               exercises.md (overview per slide), runbook.md (steps and checks)
docs/tickets/       REWE-101.md, REWE-102.md: sample tickets that simulate Jira (exercise 1)
.claude/
├── settings.json   register your hooks here
├── hooks/HookInput.java     helper: reads the hook JSON from stdin (field, flag, quote)
├── hooks/session_facts_to_preserve_after_compact.txt   sample facts for exercise 3 (compact facts)
├── agents/gbos-reviewer.md  subagent: GBOS review of AGENTS.md (read-only)
└── skills/gbos-agents-review/   skill incl. scripts/CheckAgentsMd.java (deterministic check)
```

## Exercises

The exercises come from the slides; the repo provides what they need. Overview: [docs/exercises.md](docs/exercises.md).

**Chapter 05 Hooks (slide 34):** four exercises, two optional ones for fast groups or for later

| # | Hook | Event | Task |
|---|---|---|---|
| 1 | Ticket context | `UserPromptSubmit` | When the prompt names a ticket like `REWE-101`, add the ticket from `docs/tickets/` to Claude's context. See [Tickets](#tickets-simulated-jira). |
| 2 | Prompt secret guard | `UserPromptSubmit` | Block a prompt that contains an API key, an IBAN, a card number or three or more e-mail addresses, before it reaches the model. |
| 3 | Compact facts | `SessionStart` (`compact`) | After `/compact`, print `.claude/hooks/session_facts_to_preserve_after_compact.txt` so the key project facts are back in the context. The file has sample facts; add one of your own and check that Claude still knows it after compacting. |
| 4 | GBOS gate | `PostToolUse` | After an edit of `AGENTS.md`, run `CheckAgentsMd.java` and feed its findings back to Claude, with the hint to start the subagent `gbos-reviewer`. |
| A | Harness guard (optional) | `PreToolUse` | Before Claude changes `.claude/settings*.json` or `.claude/hooks/`, ask the user (`"permissionDecision": "ask"`) and say what changes. Do this one last. |
| B | Notify (optional) | `Notification` | Show a desktop notification when Claude waits for you. Register it with `"async": true`. |

**Chapter 06 Context management**

| Slide | Task |
|---|---|
| 45 | Start `claude --settings tools/status-line/settings.json`, ask two short questions, then switch the model with `/model` and ask once more. Watch `hit` and `misses`. |
| 48 | Write `bin/RunSilent.java`: run a command, print one line when it passes, the last 60 lines when it fails. The tests print about 250 lines. |

Write the hooks as `.claude/hooks/<Name>.java`, register them in `.claude/settings.json` as `java "$CLAUDE_PROJECT_DIR/.claude/hooks/<Name>.java"`. You don't have to type the code yourself: let Claude write it, and tell it to use `.claude/hooks/HookInput.java` to read the hook JSON.

Overview with slide titles and what each exercise shows: [docs/exercises.md](docs/exercises.md). Steps, test inputs and how you know it worked: [docs/runbook.md](docs/runbook.md).

## Tickets (simulated Jira)

`docs/tickets/` holds two sample tickets for exercise 1:

| Ticket | Type | Summary |
|---|---|---|
| [REWE-101](docs/tickets/REWE-101.md) | Story | Low-stock warning in the item list |
| [REWE-102](docs/tickets/REWE-102.md) | Bug | Reject overly long item names |

They **simulate** pulling tickets from Jira, e.g. through a Jira MCP server. There is no Jira in this lab: your hook reads the Markdown file instead, so the exercise works offline and the same for everyone.

In a real project it would look like this:

1. You write `Implement REWE-101` in Claude Code.
2. The `UserPromptSubmit` hook finds the key and fetches the ticket from Jira: summary, description, acceptance criteria. A command hook is a plain program, so it calls Jira's REST API directly (`GET /rest/api/3/issue/REWE-101`), with the URL and an API token from environment variables, never from the code. That is the same data a Jira MCP server provides.
3. The hook prints the ticket. Claude Code adds it to the context, and Claude starts with the real acceptance criteria instead of your one-line prompt.

Without the hook, Claude could also fetch the ticket itself through a Jira MCP server, but only if it decides to. The hook makes it happen on every prompt that names a ticket.

## Session facts (exercise 3)

`.claude/hooks/session_facts_to_preserve_after_compact.txt` holds a few facts about the app that a compaction summary tends to drop: the port, the API routes, the demo data. Unlike the tickets, it simulates nothing: it is a real project file, versioned in git and reviewed like code. Your hook reads it after every compaction (`SessionStart`, matcher `compact`) and prints it, and Claude Code adds it to the fresh context. Add a line of your own to see it work.

**Why not in AGENTS.md?** AGENTS.md is the central, versioned place for instructions, and Claude Code loads it again after a compaction anyway: printing it from a hook would only duplicate it. The facts in this file are different: Claude *could* find them in the code, so by the GBOS rule they don't belong in AGENTS.md (keep it small, nothing inferable). But after a compaction Claude would have to read the code again to find them. The file brings them back without that.

**In a real project** you would go one of three ways:

1. A static file like this one: simple, deterministic; the team keeps it current.
2. Generated facts: the hook computes them at compaction time, e.g. the current branch and the ticket key in it, with the ticket summary fetched from Jira as in exercise 1.
3. Session state: a `PreCompact` hook saves the state of the current work (ticket, decisions, failed attempts, open TODOs) to a file *before* the compaction; the `SessionStart` hook puts it back afterwards. This is often what you really want to keep.

## Check without an LLM

```bash
java backend/InventoryTest.java
java .claude/skills/gbos-agents-review/scripts/CheckAgentsMd.java AGENTS.md
```

Test a hook alone (exit code: `echo $?` in Git Bash, `$LASTEXITCODE` in PowerShell):

```bash
echo '{"hook_event_name":"UserPromptSubmit","prompt":"Implement REWE-101"}' | java .claude/hooks/<Name>.java
```

## Troubleshooting

| Problem | Cause | Fix |
|---|---|---|
| `cannot find symbol … class Inventory` (`Symbol nicht gefunden`) | JDK older than 22 | see [Check Java first](#check-java-first) |
| `java -version` still shows 21 after installing 25 | the program your terminal runs in keeps the old `PATH` | close it completely (VS Code: all windows) and start it again |
| `BindException: Address already in use` | port 8000 is taken | Git Bash: `PORT=8001 java backend/Server.java`, PowerShell: `$env:PORT=8001; java backend/Server.java` |
| http://localhost:8000 shows `not found` | server started from another folder, e.g. `backend/` | stop it and start `java backend/Server.java` from the repo root |
| Hook does not run at all | not registered, `settings.json` is invalid JSON, or Claude started before the change | `/hooks` in Claude lists the active hooks; restart `claude` |
| Hook fails with `java: command not found` | Claude Code runs hooks in Git Bash, which does not find `java` | run `java -version` in Git Bash; fix its `PATH` |
| Test command fails in PowerShell | `\` line breaks and `$?` are Bash syntax | one line, exit code via `$LASTEXITCODE` |
| JSON from the hook is ignored | Claude Code reads JSON only on exit 0; on exit 2 it reads stderr | JSON → exit 0, message on stderr → exit 2 |
| Ticket hook prints nothing | no `REWE-<number>` in the prompt, or the ticket is read relative to the wrong folder | check the key; read `docs/tickets/` relative to `CLAUDE_PROJECT_DIR` |
| Compact facts hook has no effect | it only runs after `/compact` or auto-compaction (matcher `compact`), not on a normal start | run `/compact` |
| Every change to a hook asks for confirmation | that is your harness guard (exercise A) working | activate it after the other exercises |
| No desktop notification | the OS blocks it | macOS: allow notifications for "Script Editor"; Windows: check Focus Assist |
| Every prompt or edit takes about a second longer | each hook starts a JVM | normal |
| `AGENTS.md is not tracked by git` | the file is not committed | `git add AGENTS.md` and commit |
