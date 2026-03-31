package org.example.crawler;

import org.example.config.Config;

import java.nio.file.Path;
import java.util.List;

public class FileFilter {
    private final List<String> ignoredDirectories;
    private final List<String> ignoredExtensions;

    public FileFilter(Config config) {
        this.ignoredDirectories = config.getIgnoredDirectories();
        this.ignoredExtensions = config.getIgnoredExtensions();
    }

    public boolean isIgnoredDirectory(Path dirPath) {
        String dirName = dirPath.getFileName().toString();
        return ignoredDirectories.contains(dirName);
    }

    public boolean shouldIndex(Path filePath) {
        String extension = getExtension(filePath.getFileName().toString());
        return !ignoredExtensions.contains(extension);
    }

    public String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0) return "";
        return fileName.substring(lastDot + 1).toLowerCase();
    }
}
