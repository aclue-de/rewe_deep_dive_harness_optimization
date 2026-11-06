import java.nio.file.Path;
import java.util.List;

/**
 * Hook: GBOS gate for AGENTS.md.
 *
 * Event:  PostToolUse, matcher Edit|Write
 * Flow:   1. Not about AGENTS.md -> exit 0, nothing happens.
 *         2. Deterministic GBOS checks (script from the skill gbos-agents-review).
 *         3. Violation -> exit 2. stderr goes to Claude: findings plus the instruction
 *            to start the subagent gbos-reviewer for the content analysis.
 *
 * So the subagent is only started when the deterministic checks fail.
 * PostToolUse cannot undo the edit; it gives Claude feedback.
 */
public class GbosGate {

    public static void main(String[] args) throws Exception {
        String path = HookInput.fromStdin().field("file_path");
        if (path == null || !Path.of(path).getFileName().toString().equals("AGENTS.md")) {
            System.exit(0);
        }

        Path script = Path.of(System.getenv().getOrDefault("CLAUDE_PROJECT_DIR", "."),
                ".claude", "skills", "gbos-agents-review", "scripts", "CheckAgentsMd.java");
        Process check = new ProcessBuilder(List.of("java", script.toString(), path))
                .redirectErrorStream(true).start();
        String findings = new String(check.getInputStream().readAllBytes());
        if (check.waitFor() == 0) {
            System.exit(0);
        }

        System.err.println(findings.strip());
        System.err.println();
        System.err.println("Next step: start the subagent 'gbos-reviewer' for the full analysis "
                + "(what, where, which GBOS requirement, proposal). Only change AGENTS.md "
                + "after the user has agreed to the proposals.");
        System.exit(2); // only exit 2 sends stderr back to Claude
    }
}
