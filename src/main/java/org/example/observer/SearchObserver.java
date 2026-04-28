package org.example.observer;

import org.example.model.SearchResult;
import java.util.List;

public interface SearchObserver {
    void onSearch(String query, List<SearchResult> results);
}
