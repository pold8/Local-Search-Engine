package org.example.widget;

import org.example.model.SearchResult;

import java.util.List;

public class LogAnalyzerWidget implements Widget {

    private static final double ACTIVATION_THRESHOLD = 0.5;

    @Override
    public boolean isActivated(List<SearchResult> results, String query) {
        if (results.isEmpty()) return false;
        long logCount = results.stream()
                .filter(r -> "log".equalsIgnoreCase(r.getFileRecord().getExtension()))
                .count();
        return (double) logCount / results.size() >= ACTIVATION_THRESHOLD;
    }

    @Override
    public void display(List<SearchResult> results) {
        long logCount = results.stream()
                .filter(r -> "log".equalsIgnoreCase(r.getFileRecord().getExtension()))
                .count();

        System.out.println("┌─ Log Analyzer Widget ─────────────────────────┐");
        System.out.printf("│  %d log file(s) in results%n", logCount);

        results.stream()
                .filter(r -> "log".equalsIgnoreCase(r.getFileRecord().getExtension()))
                .forEach(r -> {
                    String preview = r.getPreview();
                    boolean hasError   = preview != null && preview.toLowerCase().contains("error");
                    boolean hasWarning = preview != null && preview.toLowerCase().contains("warn");
                    String flags = (hasError ? " [ERROR]" : "") + (hasWarning ? " [WARN]" : "");
                    System.out.printf("│  • %s%s%n", r.getFileRecord().getName(), flags);
                });

        System.out.println("└───────────────────────────────────────────────┘");
    }
}
