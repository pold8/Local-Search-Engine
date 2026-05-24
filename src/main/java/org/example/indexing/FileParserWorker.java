package org.example.indexing;

import org.example.core.PathScorer;
import org.example.crawler.FileFilter;
import org.example.model.FileRecord;
import org.example.model.IndexReport;
import org.example.parser.Extractor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class FileParserWorker implements Runnable {

    private final List<FileWork> work;
    private final List<Extractor> extractors;
    private final FileFilter filter;
    private final BlockingQueue<IndexTask> queue;
    private final IndexReport report;

    public FileParserWorker(List<FileWork> work, List<Extractor> extractors,
                            FileFilter filter, BlockingQueue<IndexTask> queue,
                            IndexReport report) {
        this.work = work;
        this.extractors = extractors;
        this.filter = filter;
        this.queue = queue;
        this.report = report;
    }

    @Override
    public void run() {
        for (FileWork fw : work) {
            try {
                String extension = filter.getExtension(fw.getPath().getFileName().toString());
                Extractor extractor = findExtractor(extension);

                FileRecord record = (extractor != null)
                        ? extractor.extract(fw.getPath())
                        : buildMetadataRecord(fw.getPath(), extension);

                if (record != null) {
                    record.setPathScore(PathScorer.score(record.getPath()));
                    queue.put(new IndexTask(record, fw.getStatus()));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                synchronized (report) {
                    report.logError(fw.getPath().toString(), e.getMessage());
                }
            }
        }

        try {
            queue.put(IndexTask.POISON_PILL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Extractor findExtractor(String extension) {
        for (Extractor e : extractors) {
            if (e.supports(extension)) return e;
        }
        return null;
    }

    private FileRecord buildMetadataRecord(Path filePath, String extension) {
        try {
            long size = Files.size(filePath);
            long lastModified = Files.getLastModifiedTime(filePath).toMillis();
            String fileHash = org.example.util.HashUtil.computeSHA256(filePath);
            String name = filePath.getFileName().toString();
            return new FileRecord(
                    filePath.toAbsolutePath().toString(),
                    name, extension, size, lastModified, fileHash, "", "");
        } catch (Exception e) {
            return null;
        }
    }
}
