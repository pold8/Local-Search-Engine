package org.example.model;

import java.util.List;

public class DuplicateGroup {
    private final String fileHash;
    private final long size;
    private final List<String> paths;

    public DuplicateGroup(String fileHash, long size, List<String> paths) {
        this.fileHash = fileHash;
        this.size = size;
        this.paths = paths;
    }

    public String getFileHash() {
        return fileHash;
    }

    public long getSize() {
        return size;
    }

    public List<String> getPaths() {
        return paths;
    }
}
