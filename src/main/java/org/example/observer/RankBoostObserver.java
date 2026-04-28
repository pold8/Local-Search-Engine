package org.example.observer;

import org.example.db.SearchHistoryRepository;
import org.example.model.SearchResult;
import java.util.List;

public class RankBoostObserver implements SearchObserver {
    private final SearchHistoryRepository historyRepository;

    public RankBoostObserver(SearchHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public void onSearch(String query, List<SearchResult> results) {
        int frequency = historyRepository.getQueryFrequency(query);
        if (frequency > 0) {
            for (SearchResult result : results) {
                result.addRankBoost(frequency);
            }
        }
    }
}
