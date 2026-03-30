package org.example.parser;

import org.example.db.FileRepository;
import org.example.model.FileRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

public class ChangeDetector {

    public enum FileStatus {
        NEW,        // never been indexed
        MODIFIED,   // indexed before but last_modified has changed
        UNCHANGED   // indexed before and timestamps match
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

            long diskTimestamp = Files.getLastModifiedTime(filePath).toMillis();
            long dbTimestamp = stored.getLastModified();

            if (diskTimestamp == dbTimestamp) {
                return FileStatus.UNCHANGED;
            } else {
                return FileStatus.MODIFIED;
            }

        } catch (SQLException e) {
            System.err.println("[ChangeDetector] DB error checking file: " + filePath + " — " + e.getMessage());
            return FileStatus.NEW;
        } catch (IOException e) {
            System.err.println("[ChangeDetector] Cannot read timestamp: " + filePath + " — " + e.getMessage());
            return FileStatus.NEW;
        }
    }
}
