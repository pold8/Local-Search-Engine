package org.example.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParsedQuery {

    private final List<String> contentTerms;
    private final List<String> pathTerms;
    private final String colorTerm;

    private final String rawQuery;

    public ParsedQuery(String rawQuery,
            List<String> contentTerms,
            List<String> pathTerms,
            String colorTerm) {
        this.rawQuery = rawQuery;
        this.contentTerms = Collections.unmodifiableList(new ArrayList<>(contentTerms));
        this.pathTerms = Collections.unmodifiableList(new ArrayList<>(pathTerms));
        this.colorTerm = colorTerm;
    }

    public List<String> getContentTerms() {
        return contentTerms;
    }

    public List<String> getPathTerms() {
        return pathTerms;
    }

    public String getColorTerm() {
        return colorTerm;
    }

    public String getRawQuery() {
        return rawQuery;
    }

    public boolean isQualified() {
        return !contentTerms.isEmpty() || !pathTerms.isEmpty() || colorTerm != null;
    }
}
