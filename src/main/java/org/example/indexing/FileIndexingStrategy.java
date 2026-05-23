package org.example.indexing;

import org.example.model.FileRecord;

import java.io.File;

public interface FileIndexingStrategy {

    boolean supports(String extension);

    FileRecord process(File file);
}
