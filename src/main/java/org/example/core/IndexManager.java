package org.example.core;

import org.example.config.Config;
import org.example.crawler.Crawler;
import org.example.crawler.FileFilter;
import org.example.db.FileRepository;
import org.example.indexing.DatabaseWriterWorker;
import org.example.indexing.FileParserWorker;
import org.example.indexing.FileWork;
import org.example.indexing.IndexTask;
import org.example.model.IndexReport;
import org.example.parser.ChangeDetector;
import org.example.parser.ChangeDetector.FileStatus;
import org.example.parser.Extractor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class IndexManager {

    private final Config config;
    private final Crawler crawler;
    private final FileFilter filter;
    private final List<Extractor> extractors;
    private final ChangeDetector changeDetector;
    private final FileRepository repository;

    public IndexManager(Config config, Crawler crawler, FileFilter filter,
                        List<Extractor> extractors, ChangeDetector changeDetector,
                        FileRepository repository) {
        this.config = config;
        this.crawler = crawler;
        this.filter = filter;
        this.extractors = extractors;
        this.changeDetector = changeDetector;
        this.repository = repository;
    }

    public IndexReport index() {
        IndexReport report = new IndexReport();
        System.out.println("[Indexer] Starting indexing from: " + config.getRootDirectory());

        List<Path> files = crawler.crawl(config.getRootDirectory(), report);
        System.out.println("[Indexer] Found " + files.size() + " files to process.");

        // Phase 1: determine status for each file — single-threaded because DB reads
        // on a shared Connection are not safe to issue concurrently from multiple threads.
        List<FileWork> workItems = new ArrayList<>();
        for (Path filePath : files) {
            FileStatus status = changeDetector.getStatus(filePath);
            if (status == FileStatus.UNCHANGED) {
                report.incrementSkipped();
            } else {
                workItems.add(new FileWork(filePath, status));
            }
        }

        if (workItems.isEmpty()) {
            report.finish();
            System.out.println("[Indexer] Nothing to index — all files unchanged.");
            return report;
        }

        // Phase 2: split work across N producer threads (CPU-bound file parsing)
        int workerCount = Math.min(
                Math.max(1, Runtime.getRuntime().availableProcessors()),
                workItems.size());

        BlockingQueue<IndexTask> queue = new LinkedBlockingQueue<>();
        List<List<FileWork>> partitions = partition(workItems, workerCount);

        // Phase 3: start the single writer thread (consumer); it owns the transaction.
        DatabaseWriterWorker writer = new DatabaseWriterWorker(queue, repository, report, workerCount);
        Thread writerThread = new Thread(writer, "db-writer");
        writerThread.start();

        // Phase 4: submit producer workers to a fixed thread pool.
        ExecutorService executor = Executors.newFixedThreadPool(workerCount);
        for (List<FileWork> partition : partitions) {
            executor.submit(new FileParserWorker(partition, extractors, filter, queue, report));
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            writerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Indexer] Interrupted while waiting for workers.");
        }

        report.finish();
        System.out.println("[Indexer] Indexing complete.");
        return report;
    }

    private static <T> List<List<T>> partition(List<T> list, int n) {
        List<List<T>> parts = new ArrayList<>();
        int size = list.size();
        int base = size / n;
        int remainder = size % n;
        int start = 0;
        for (int i = 0; i < n; i++) {
            int end = start + base + (i < remainder ? 1 : 0);
            if (start < end) parts.add(list.subList(start, end));
            start = end;
        }
        return parts;
    }
}
