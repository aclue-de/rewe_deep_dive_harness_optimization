import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Status line for slide 45: shows how the session uses the prompt cache.
 *
 * Claude Code sends session data as JSON on stdin and shows the first line this prints.
 * Start: claude --settings tools/status-line/settings.json   (needs Claude Code 2.1.260 or later)
 *
 * Fields: read / write = tokens read from / written to the cache by the last request,
 *         hit = share of all input tokens served from the cache this session,
 *         misses = requests that re-wrote content the cache already held.
 */
public class StatusLine {

    public static void main(String[] args) throws Exception {
        String json = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        String model = text(json, "\"model\"\\s*:\\s*\\{[^}]*?\"display_name\"\\s*:\\s*\"([^\"]*)\"");
        String read = number(json, "cache_read_input_tokens");
        String write = number(json, "cache_creation_input_tokens");
        String hit = number(json, "hit_ratio");
        String misses = number(json, "misses");

        String line = "[" + (model == null ? "?" : model) + "] ";
        if (hit == null && misses == null) {
            line += "cache: waiting for the first answer";
        } else {
            line += "read " + tokens(read) + " | write " + tokens(write)
                    + " | hit " + percent(hit) + " | misses " + (misses == null ? "0" : misses);
        }
        System.out.println(line);
    }

    static String number(String json, String key) {
        return text(json, "\"" + key + "\"\\s*:\\s*([0-9.]+)");
    }

    static String text(String json, String regex) {
        Matcher m = Pattern.compile(regex).matcher(json);
        return m.find() ? m.group(1) : null;
    }

    static String tokens(String value) {
        if (value == null) {
            return "0";
        }
        long n = Math.round(Double.parseDouble(value));
        return n < 1000 ? String.valueOf(n) : String.format(java.util.Locale.ROOT, "%.1fk", n / 1000.0);
    }

    static String percent(String ratio) {
        return ratio == null ? "-" : Math.round(Double.parseDouble(ratio) * 100) + "%";
    }
}
