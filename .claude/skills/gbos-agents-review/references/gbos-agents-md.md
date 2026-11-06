# GBOS-AI-SWE-AGENTS-MD – summary

> Confidential – REWE Group internal. Paraphrased summary of the GBOS chapter
> "GBOS-AI-SWE: Artificial Intelligence in Software Engineering".
> The authoritative source is the GBOS page in Confluence.

Level: **MUST**

## Normative requirements

- Every repository where AI agents are used contains an `AGENTS.md` in the repository root, under version control.
- Should not exceed 200 lines.
- Plain Markdown, no tool-specific syntax, no `@`-imports.
- Changes go through pull request and review.
- Behavioral boundaries in three tiers:
  - **Always:** actions the agent performs without asking
  - **Ask first:** actions that need explicit human approval
  - **Never:** actions the agent must not perform
- A failure protocol: stop retrying, summarize what failed and why, request the specific human input needed.

## Why AGENTS.md and not a tool-specific file

AGENTS.md is a tool-agnostic open standard. Tool-specific files (CLAUDE.md, .cursorrules, GEMINI.md) fragment project knowledge across tools.

## Recommended creation process

Structured elicitation instead of free repo scanning or autonomous generation. The agent asks targeted questions, e.g.:
- What must never happen?
- Which build or test commands are missing from the standard scripts?
- Which parts of the codebase have exceptions to the standard rules?
- What requires human approval before the agent acts?

Then it structures the answers and drops anything it can infer from the repository.

## Recommended content (only what the agent cannot derive)

- Short application and domain summary (2–3 lines)
- Domain invariants and non-obvious constraints
- Team-wide coding standards not enforced by tooling
- Project-specific build and test commands with context (flags, profiles)
- An instruction to derive framework, language and infrastructure versions from build and configuration files
- Boundaries as specific as possible (name the command, file or action)

## Quality constraints

Avoid inferable content:
- plain, uncommented directory structures
- style rules already enforced by a linter or formatter
- technology choices already declared in manifest files

Target 100–120 lines. Files close to 200 lines almost always contain inferable content.

## Maintenance

Audit regularly. For every line ask:
1. Still true?
2. Still relevant?
3. Contradicts another instruction?
4. Still not inferable from the repo?

If any answer is "no": remove or update. If the team repeatedly corrects the same agent behavior, add the missing constraint.

**AGENTS.md is a nudge, not a guarantee.** Automated guardrails (linters, CI checks, pre-commit hooks) remain the primary enforcement mechanism; AGENTS.md supplements them where mechanical enforcement is not possible.

## Rationale (short)

- Minimal, focused instruction files reduce agent runtime and token usage.
- Files that duplicate what the agent can derive reduce task success and increase cost.
- A stale AGENTS.md degrades performance; deleting it is better than leaving it outdated.

## Compliance

Automatic checks (see `scripts/CheckAgentsMd.java`):
- file exists in repo root and is tracked
- at most 200 lines
- no tool-specific syntax (`@workspace`, `@codebase`, `@terminal`, `@cursor`, `@file`, `#file`)
- contains a section "Failure Protocol"
- changes only via pull request

Manual review:
- every boundary names a specific action, tool, command or file
- boundary tiers include concrete examples where behavior is non-obvious
- failure protocol: stop, summarize, request human input
- only non-inferable content

## Anti-patterns (typical non-compliant file)

- "This is a <framework> application written in <language>" (inferable)
- tool-specific syntax such as `@workspace`
- a copied directory tree
- vague guidelines ("write clean code", "be careful with security", "follow SOLID")
- style rules the linter already enforces
- no failure protocol
