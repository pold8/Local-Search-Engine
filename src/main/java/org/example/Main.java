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
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            Config config = Config.load("config.json");
            config.validate();

            DatabaseManager db = new DatabaseManager(config.getDatabasePath());
            db.initialize();

            FileRepository repository = new FileRepository(db.getConnection());

            printWelcome();

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("\n> ");
                String line = scanner.nextLine().trim();

                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+", 2);
                String command = parts[0].toLowerCase();

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
                        if (parts.length < 2 || parts[1].isBlank()) {
                            System.out.println("Missing search query.");
                            System.out.println("Usage: search <query>");
                            continue;
                        }

                        QueryEngine queryEngine = new QueryEngine(repository);
                        queryEngine.search(parts[1]);
                    }

                    case "help" -> printHelp();

                    case "exit", "quit" -> {
                        System.out.println("Goodbye!");
                        db.close();
                        return;
                    }

                    default -> {
                        System.out.println("Unknown command: " + command);
                        printHelp();
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printWelcome() {
        String menu = """
                
                ╔══════════════════════════════════════════╗
                ║       Local Search Engine — CLI          ║
                ╚══════════════════════════════════════════╝
                
                Available commands:
                  index              Index files from the configured root directory
                  search <query>     Search indexed files for the given query
                  help               Show this help message
                  exit               Exit the application
                
                Examples:
                  search Main.java
                  search database connection
                  search TODO
                  index""";
        System.out.println(menu);
    }

    private static void printHelp() {
        String help = """
                Available commands:
                  index              Index files from the configured root directory
                  search <query>     Search indexed files for the given query
                  help               Show this help message
                  exit               Exit the application""";
        System.out.println(help);
    }
}