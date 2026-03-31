package org.example.core;

import org.example.db.FileRepository;
import org.example.model.SearchResult;

import java.sql.SQLException;
import java.util.List;

public class QueryEngine {

    private final FileRepository repository;

    public QueryEngine(FileRepository repository) {
        this.repository = repository;
    }

    public void search(String query) {
        try {
            List<SearchResult> results = repository.search(query);

            if (results.isEmpty()) {
                System.out.println("No results found for: \"" + query + "\"");
                return;
            }

            System.out.println("\nSearch results for: \"" + query + "\"");
            System.out.println("─".repeat(50));

            for (SearchResult result : results) {
                System.out.println(result);
                System.out.println();
            }

            System.out.println("─".repeat(50));
            System.out.println("Total: " + results.size() + " result(s)");

        } catch (SQLException e) {
            System.err.println("[Search] Error executing query: " + e.getMessage());
        }
    }
}