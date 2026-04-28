package org.example.observer;

import org.example.db.SearchHistoryRepository;
import org.example.model.SearchResult;
import java.util.List;

public class HistoryObserver implements SearchObserver {
    private final SearchHistoryRepository historyRepository;

    public HistoryObserver(SearchHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public void onSearch(String query, List<SearchResult> results) {
        historyRepository.saveSearch(query);
    }
}
