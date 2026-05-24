package org.example.indexing;

import org.example.db.FileRepository;
import org.example.model.IndexReport;
import org.example.parser.ChangeDetector.FileStatus;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

public class DatabaseWriterWorker implements Runnable {

    private final BlockingQueue<IndexTask> queue;
    private final FileRepository repository;
    private final IndexReport report;
    private final int producerCount;

    public DatabaseWriterWorker(BlockingQueue<IndexTask> queue, FileRepository repository,
                                IndexReport report, int producerCount) {
        this.queue = queue;
        this.repository = repository;
        this.report = report;
        this.producerCount = producerCount;
    }

    @Override
    public void run() {
        int poisonsReceived = 0;
        int committed = 0;

        try {
            repository.beginTransaction();

            while (poisonsReceived < producerCount) {
                IndexTask task = queue.take();

                if (task.isPoisonPill()) {
                    poisonsReceived++;
                    continue;
                }

                try {
                    if (task.getStatus() == FileStatus.NEW) {
                        repository.save(task.getRecord());
                    } else {
                        repository.update(task.getRecord());
                    }
                    report.incrementIndexed();
                    committed++;

                    if (committed % 100 == 0) {
                        System.out.println("[Writer] Committed " + committed + " files...");
                    }
                } catch (SQLException e) {
                    synchronized (report) {
                        report.logError(task.getRecord().getPath(), "DB error: " + e.getMessage());
                    }
                }
            }

            repository.commitTransaction();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            repository.rollbackTransaction();
        } catch (SQLException e) {
            System.err.println("[Writer] Transaction error: " + e.getMessage());
            repository.rollbackTransaction();
        }
    }
}
