package org.example.indexing;

import org.example.parser.ChangeDetector.FileStatus;

import java.nio.file.Path;

public class FileWork {

    private final Path path;
    private final FileStatus status;

    public FileWork(Path path, FileStatus status) {
        this.path = path;
        this.status = status;
    }

    public Path getPath()       { return path; }
    public FileStatus getStatus() { return status; }
}
