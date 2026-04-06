package org.example.parser;

import org.example.db.FileRepository;
import org.example.model.FileRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

public class ChangeDetector {

    public enum FileStatus {
        NEW,
        MODIFIED,
        UNCHANGED
    }

    private final FileRepository repository;

    public ChangeDetector(FileRepository repository) {
        this.repository = repository;
    }

    public FileStatus getStatus(Path filePath) {
        try {
            FileRecord stored = repository.findByPath(filePath.toAbsolutePath().toString());
            if (stored == null) {
                return FileStatus.NEW;
            }

            String diskHash = org.example.util.HashUtil.computeSHA256(filePath);
            String dbHash = stored.getFileHash();

            if (dbHash != null && diskHash.equals(dbHash)) {
                return FileStatus.UNCHANGED;
            } else {
                return FileStatus.MODIFIED;
            }

        } catch (SQLException e) {
            System.err.println("[ChangeDetector] DB error checking file: " + filePath + " — " + e.getMessage());
            return FileStatus.NEW;
        }
    }
}
