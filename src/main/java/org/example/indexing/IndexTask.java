package org.example.indexing;

import org.example.model.FileRecord;
import org.example.parser.ChangeDetector.FileStatus;

public class IndexTask {

    public static final IndexTask POISON_PILL = new IndexTask(null, null);

    private final FileRecord record;
    private final FileStatus status;

    public IndexTask(FileRecord record, FileStatus status) {
        this.record = record;
        this.status = status;
    }

    public FileRecord getRecord()   { return record; }
    public FileStatus getStatus()   { return status; }
    public boolean isPoisonPill()   { return this == POISON_PILL; }
}
