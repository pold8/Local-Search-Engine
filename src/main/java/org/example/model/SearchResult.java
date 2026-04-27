package org.example.model;

public class SearchResult {
    private final FileRecord fileRecord;
    private final String preview;
    private final int rank;
    private final String matchReason;

    public SearchResult(FileRecord fileRecord, String preview, int rank, String matchReason) {
        this.fileRecord = fileRecord;
        this.preview = preview;
        this.rank = rank;
        this.matchReason = matchReason;
    }

    public FileRecord getFileRecord() { return fileRecord; }
    public String getPreview() { return preview; }
    public int getRank() { return rank; }
    public String getMatchReason() { return matchReason; }

    @Override
    public String toString() {
        return String.format("[%d] %s — %s (%s) [score: %.3f]%n     Matched by: %s%n     Preview: %s",
                rank,
                fileRecord.getName(),
                fileRecord.getPath(),
                formatSize(fileRecord.getSize()),
                fileRecord.getPathScore(),
                matchReason,
                preview != null ? preview.replace("\n", "\n     ") : "(no preview)");
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
