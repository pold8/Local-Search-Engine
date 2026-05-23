package org.example.indexing;

import org.example.model.FileRecord;
import org.example.parser.Extractor;

import java.nio.file.Path;
import java.util.List;

public class FileProcessor implements Extractor {

    private final List<FileIndexingStrategy> strategies;

    public FileProcessor(List<FileIndexingStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public boolean supports(String extension) {
        return strategies.stream().anyMatch(s -> s.supports(extension));
    }

    @Override
    public FileRecord extract(Path filePath) {
        String ext = getExtension(filePath.getFileName().toString());
        for (FileIndexingStrategy strategy : strategies) {
            if (strategy.supports(ext)) {
                return strategy.process(filePath.toFile());
            }
        }
        return null;
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0) return "";
        return fileName.substring(lastDot + 1).toLowerCase();
    }
}
