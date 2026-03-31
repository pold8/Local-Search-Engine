package org.example;

import org.example.config.Config;
import org.example.core.IndexManager;
import org.example.core.QueryEngine;
import org.example.crawler.Crawler;
import org.example.crawler.FileFilter;
import org.example.db.DatabaseManager;
import org.example.db.FileRepository;
import org.example.model.IndexReport;
import org.example.parser.ChangeDetector;
import org.example.parser.Extractor;
import org.example.parser.FileParser;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }

        try {
            Config config = Config.load("config.json");
            config.validate();

            DatabaseManager db = new DatabaseManager(config.getDatabasePath());
            db.initialize();

            FileRepository repository = new FileRepository(db.getConnection());

            String command = args[0];

            switch (command) {
                case "index" -> {
                    FileFilter filter = new FileFilter(config);
                    Crawler crawler = new Crawler(filter);
                    ChangeDetector changeDetector = new ChangeDetector(repository);
                    Extractor textExtractor = new FileParser(config.getTextExtensions());

                    IndexManager indexManager = new IndexManager(
                            config, crawler, filter,
                            List.of(textExtractor),
                            changeDetector, repository);

                    IndexReport report = indexManager.index();
                    System.out.println(report.generateReport());
                }

                case "search" -> {
                    if (args.length < 2) {
                        System.out.println("Missing search query.");
                        System.out.println("Usage: search <query>");
                        return;
                    }

                    String query = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

                    QueryEngine queryEngine = new QueryEngine(repository);
                    queryEngine.search(query);
                }

                default -> {
                    System.out.println("Unknown command: " + command);
                    printUsage();
                }
            }

            db.close();

        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("Local Search Engine — CLI");
        System.out.println("Usage:");
        System.out.println("  index              Index files from the configured root directory");
        System.out.println("  search <query>     Search indexed files for the given query");
    }
}