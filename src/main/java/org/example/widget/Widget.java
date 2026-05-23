package org.example.widget;

import org.example.model.SearchResult;

import java.util.List;

public interface Widget {

    boolean isActivated(List<SearchResult> results, String query);

    void display(List<SearchResult> results);
}
