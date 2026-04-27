package org.example.core;

import org.example.db.FileRepository;
import org.example.model.SearchResult;
import org.example.parser.ParsedQuery;
import org.example.parser.QueryParser;

import java.sql.SQLException;
import java.util.List;

public class QueryEngine {

    private final FileRepository repository;

    public QueryEngine(FileRepository repository) {
        this.repository = repository;
    }

    public void search(String rawQuery) {
        try {
            ParsedQuery parsedQuery = QueryParser.parse(rawQuery);
            List<SearchResult> results = repository.searchParsed(parsedQuery);

            if (results.isEmpty()) {
                System.out.println("No results found for: " + formatHeader(parsedQuery));
                return;
            }

            System.out.println("\nSearch results for: " + formatHeader(parsedQuery));
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

    /**
     * Returns a human-readable header describing the active search terms.
     * <p>
     * Qualified example: {@code content:[java, homework] path:[Documents]}
     * <br>Plain example:  {@code "java"}
     */
    private String formatHeader(ParsedQuery query) {
        if (!query.isQualified()) {
            return "\"" + query.getRawQuery() + "\"";
        }

        StringBuilder sb = new StringBuilder();

        if (!query.getContentTerms().isEmpty()) {
            sb.append("content:").append(query.getContentTerms());
        }

        if (!query.getPathTerms().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("path:").append(query.getPathTerms());
        }

        return sb.toString();
    }
}