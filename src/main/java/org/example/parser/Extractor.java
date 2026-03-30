package org.example.parser;

import org.example.model.FileRecord;

import java.nio.file.Path;

public interface Extractor {

    boolean supports(String extension);

    FileRecord extract(Path filePath);
}
