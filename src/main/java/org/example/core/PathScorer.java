package org.example.core;

import java.util.Set;

public class PathScorer {

    private static final Set<String> IMPORTANT_DIRS = Set.of(
            "Documents", "Desktop", "src", "Projects", "Downloads");

    private static final Set<String> BONUS_EXTENSIONS = Set.of(
            ".java", ".md", ".txt", ".py", ".js", ".ts");

    private static final Set<String> PENALTY_EXTENSIONS = Set.of(
            ".log", ".tmp", ".cache", ".class");

    private PathScorer() {
    }

    public static double score(String absolutePath) {
        if (absolutePath == null || absolutePath.isBlank()) {
            return 0.0;
        }

        int depth = Math.max(1, countSeparators(absolutePath));
        double s = 1.0 / depth;

        for (String dir : IMPORTANT_DIRS) {
            if (absolutePath.contains("/" + dir + "/")
                    || absolutePath.contains("/" + dir)
                    || absolutePath.startsWith(dir + "/")) {
                s += 0.3;
                break;
            }
        }

        String ext = extractExtension(absolutePath);
        if (BONUS_EXTENSIONS.contains(ext)) {
            s += 0.2;
        } else if (PENALTY_EXTENSIONS.contains(ext)) {
            s -= 0.2;
        }

        return Math.max(0.0, Math.min(1.0, s));
    }

    private static int countSeparators(String path) {
        int count = 0;
        for (char c : path.toCharArray()) {
            if (c == '/')
                count++;
        }
        return count;
    }

    private static String extractExtension(String path) {
        int lastSlash = path.lastIndexOf('/');
        String filename = (lastSlash >= 0) ? path.substring(lastSlash + 1) : path;
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1)
            return "";
        return filename.substring(dot).toLowerCase();
    }
}
