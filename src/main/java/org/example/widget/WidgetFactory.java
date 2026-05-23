package org.example.widget;

import org.example.model.SearchResult;

import java.util.List;

public class WidgetFactory {

    private final List<Widget> registered;

    public WidgetFactory(List<Widget> registered) {
        this.registered = registered;
    }

    public List<Widget> getActivated(List<SearchResult> results, String query) {
        return registered.stream()
                .filter(w -> w.isActivated(results, query))
                .toList();
    }
}
