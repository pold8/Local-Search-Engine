package org.example.indexing;

import org.example.model.FileRecord;
import org.example.parser.FileParser;

import java.io.File;
import java.util.List;

public class TextFileStrategy implements FileIndexingStrategy {

    private final FileParser parser;

    public TextFileStrategy(List<String> textExtensions) {
        this.parser = new FileParser(textExtensions);
    }

    @Override
    public boolean supports(String extension) {
        return parser.supports(extension);
    }

    @Override
    public FileRecord process(File file) {
        return parser.extract(file.toPath());
    }
}
