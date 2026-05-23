package org.example.query;

import java.util.Map;

public class SynonymDecorator implements QueryBuilder {

    private static final Map<String, String> SYNONYMS = Map.of(
            "img",   "img OR image OR photo",
            "doc",   "doc OR document",
            "vid",   "vid OR video OR movie",
            "cfg",   "cfg OR config OR configuration",
            "src",   "src OR source",
            "lib",   "lib OR library"
    );

    private final QueryBuilder wrapped;

    public SynonymDecorator(QueryBuilder wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String build(String rawQuery) {
        String base = wrapped.build(rawQuery);
        String[] tokens = base.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) sb.append(" ");
            String lower = tokens[i].toLowerCase();
            sb.append(SYNONYMS.getOrDefault(lower, tokens[i]));
        }
        return sb.toString();
    }
}
