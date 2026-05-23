package org.example.core;

import org.example.db.FileRepository;
import org.example.model.SearchResult;
import org.example.parser.ParsedQuery;
import org.example.parser.QueryParser;
import org.example.db.SearchHistoryRepository;
import org.example.observer.SearchObserver;
import org.example.query.BaseQueryBuilder;
import org.example.query.QueryBuilder;
import org.example.ranking.RankingStrategy;
import org.example.ranking.RelevanceRankingStrategy;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QueryEngine {

    private final FileRepository repository;
    private final SearchHistoryRepository historyRepository;
    private RankingStrategy currentStrategy;
    private final List<SearchObserver> observers = new ArrayList<>();
    private QueryBuilder queryBuilder = new BaseQueryBuilder();

    public QueryEngine(FileRepository repository, SearchHistoryRepository historyRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.currentStrategy = new RelevanceRankingStrategy();
    }

    public void setQueryBuilder(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public void addObserver(SearchObserver observer) {
        this.observers.add(observer);
    }

    public void setRankingStrategy(RankingStrategy strategy) {
        this.currentStrategy = strategy;
        this.repository.setRankingStrategy(strategy);
    }

    private void notifyObservers(String query, List<SearchResult> results) {
        for (SearchObserver observer : observers) {
            observer.onSearch(query, results);
        }
    }

    private void showSuggestions(String rawQuery) {
        String prefix = rawQuery.split("\\s+")[0];
        List<String> suggestions = historyRepository.getSuggestions(prefix);
        if (!suggestions.isEmpty()) {
            List<String> formatted = new ArrayList<>();
            for (String s : suggestions) formatted.add(s + " (try this)");
            System.out.println("Suggestions: " + String.join(", ", formatted));
            System.out.println();
        }
    }

    /** Returns a user-friendly label for the active strategy. */
    public String getCurrentStrategyName() {
        return currentStrategy.getClass().getSimpleName()
                .replace("RankingStrategy", "")
                .toLowerCase();
    }

    public List<SearchResult> search(String rawQuery) {
        try {
            String queryToProcess = hasQualifiers(rawQuery) ? rawQuery : queryBuilder.build(rawQuery);
            ParsedQuery parsedQuery = QueryParser.parse(queryToProcess);
            List<SearchResult> results = repository.searchParsed(parsedQuery);

            notifyObservers(rawQuery, results);
            showSuggestions(rawQuery);

            if (results.isEmpty()) {
                System.out.println("No results found for: " + formatHeader(parsedQuery));
                return results;
            }

            System.out.println("\nSearch results for: " + formatHeader(parsedQuery)
                    + "  [ranked by: " + getCurrentStrategyName() + "]");
            System.out.println("─".repeat(50));

            for (SearchResult result : results) {
                System.out.println(result);
                System.out.println();
            }

            System.out.println("─".repeat(50));
            System.out.println("Total: " + results.size() + " result(s)");

            return results;

        } catch (SQLException e) {
            System.err.println("[Search] Error executing query: " + e.getMessage());
            return List.of();
        }
    }

    private boolean hasQualifiers(String rawQuery) {
        if (rawQuery == null) return false;
        String lower = rawQuery.toLowerCase();
        return lower.contains("content:") || lower.contains("path:") || lower.contains("color:");
    }

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

        if (query.getColorTerm() != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("color:").append(query.getColorTerm());
        }

        return sb.toString();
    }
}