package org.example.core;

import java.util.Set;

/**
 * Computes a relevance score [0.0, 1.0] for a file based on its absolute path.
 *
 * <p>Scoring components (applied in order, then clamped):
 * <ol>
 *   <li><b>Depth penalty</b> – {@code 1.0 / depth} (shallower = higher score)</li>
 *   <li><b>Important directory bonus</b> – +0.3 if the path passes through a
 *       known-important directory</li>
 *   <li><b>Extension bonus</b>  – +0.2 for recognised source/document extensions</li>
 *   <li><b>Extension penalty</b> – −0.2 for noisy/generated file extensions</li>
 * </ol>
 */
public class PathScorer {

    private static final Set<String> IMPORTANT_DIRS = Set.of(
            "Documents", "Desktop", "src", "Projects", "University", "Downloads"
    );

    private static final Set<String> BONUS_EXTENSIONS = Set.of(
            ".java", ".md", ".txt", ".py", ".js", ".ts"
    );

    private static final Set<String> PENALTY_EXTENSIONS = Set.of(
            ".log", ".tmp", ".cache", ".class"
    );

    private PathScorer() { /* utility class */ }

    /**
     * Scores {@code absolutePath} and returns a value in {@code [0.0, 1.0]}.
     *
     * @param absolutePath the full, absolute path of the file
     * @return a score between 0.0 (worst) and 1.0 (best)
     */
    public static double score(String absolutePath) {
        if (absolutePath == null || absolutePath.isBlank()) {
            return 0.0;
        }

        // ── Depth penalty ──────────────────────────────────────────────────
        // Count the number of '/' separators; minimum 1 to avoid division by zero.
        int depth = Math.max(1, countSeparators(absolutePath));
        double s = 1.0 / depth;

        // ── Important directory bonus ──────────────────────────────────────
        for (String dir : IMPORTANT_DIRS) {
            if (absolutePath.contains("/" + dir + "/")
                    || absolutePath.contains("/" + dir)
                    || absolutePath.startsWith(dir + "/")) {
                s += 0.3;
                break; // apply bonus only once even if multiple matches
            }
        }

        // ── Extension bonus / penalty ──────────────────────────────────────
        String ext = extractExtension(absolutePath);
        if (BONUS_EXTENSIONS.contains(ext)) {
            s += 0.2;
        } else if (PENALTY_EXTENSIONS.contains(ext)) {
            s -= 0.2;
        }

        // ── Clamp to [0.0, 1.0] ───────────────────────────────────────────
        return Math.max(0.0, Math.min(1.0, s));
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private static int countSeparators(String path) {
        int count = 0;
        for (char c : path.toCharArray()) {
            if (c == '/') count++;
        }
        return count;
    }

    private static String extractExtension(String path) {
        int lastSlash = path.lastIndexOf('/');
        String filename = (lastSlash >= 0) ? path.substring(lastSlash + 1) : path;
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot).toLowerCase();
    }
}
