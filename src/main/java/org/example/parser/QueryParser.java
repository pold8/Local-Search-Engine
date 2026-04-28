package org.example.parser;

import java.util.ArrayList;
import java.util.List;

public class QueryParser {

    private static final String CONTENT_PREFIX = "content:";
    private static final String PATH_PREFIX = "path:";

    private QueryParser() {
    }

    public static ParsedQuery parse(String rawQuery) {
        List<String> contentTerms = new ArrayList<>();
        List<String> pathTerms = new ArrayList<>();

        if (rawQuery == null || rawQuery.isBlank()) {
            return new ParsedQuery("", contentTerms, pathTerms);
        }

        String[] tokens = rawQuery.trim().split("\\s+");

        for (String token : tokens) {
            String lower = token.toLowerCase();

            if (lower.startsWith(CONTENT_PREFIX)) {
                String value = token.substring(CONTENT_PREFIX.length());
                if (!value.isBlank()) {
                    contentTerms.add(value);
                }
            } else if (lower.startsWith(PATH_PREFIX)) {
                String value = token.substring(PATH_PREFIX.length());
                if (!value.isBlank()) {
                    pathTerms.add(value);
                }
            }
        }

        return new ParsedQuery(rawQuery, contentTerms, pathTerms);
    }
}
