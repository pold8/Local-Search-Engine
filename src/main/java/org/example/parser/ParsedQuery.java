package org.example.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the result of parsing a raw search query.
 * <p>
 * A "qualified" query contains at least one {@code content:} or {@code path:}
 * token. An unqualified query falls back to the original plain-text search.
 */
public class ParsedQuery {

    private final List<String> contentTerms;
    private final List<String> pathTerms;

    /** Raw query string kept for fall-back / display purposes. */
    private final String rawQuery;

    public ParsedQuery(String rawQuery,
                       List<String> contentTerms,
                       List<String> pathTerms) {
        this.rawQuery     = rawQuery;
        this.contentTerms = Collections.unmodifiableList(new ArrayList<>(contentTerms));
        this.pathTerms    = Collections.unmodifiableList(new ArrayList<>(pathTerms));
    }

    /** All values that appeared after a {@code content:} qualifier. */
    public List<String> getContentTerms() { return contentTerms; }

    /** All values that appeared after a {@code path:} qualifier. */
    public List<String> getPathTerms() { return pathTerms; }

    /** The original, unmodified query string entered by the user. */
    public String getRawQuery() { return rawQuery; }

    /**
     * Returns {@code true} when the query contains at least one qualifier
     * ({@code content:} or {@code path:}).
     */
    public boolean isQualified() {
        return !contentTerms.isEmpty() || !pathTerms.isEmpty();
    }
}
