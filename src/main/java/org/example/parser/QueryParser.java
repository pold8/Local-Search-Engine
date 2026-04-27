package org.example.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses a raw search query string into a {@link ParsedQuery}.
 *
 * <p>Supported syntax:
 * <pre>
 *   search content:java path:Documents     → qualified query
 *   search content:java content:homework   → multiple content terms (AND)
 *   search java                            → plain query (backward compatible)
 * </pre>
 *
 * <p>Rules:
 * <ul>
 *   <li>Tokens are split on whitespace.</li>
 *   <li>{@code content:<value>} tokens append to {@code contentTerms}.</li>
 *   <li>{@code path:<value>} tokens append to {@code pathTerms}.</li>
 *   <li>Bare tokens (no qualifier) are only used when <em>no</em> qualifier is
 *       found anywhere in the query — in that case the whole raw query is
 *       passed through unchanged for backward-compatible plain search.</li>
 * </ul>
 */
public class QueryParser {

    private static final String CONTENT_PREFIX = "content:";
    private static final String PATH_PREFIX    = "path:";

    private QueryParser() { /* utility class */ }

    /**
     * Parses {@code rawQuery} and returns the corresponding {@link ParsedQuery}.
     *
     * @param rawQuery the full query string typed by the user (without the
     *                 leading "search " command word)
     * @return a {@link ParsedQuery} instance; never {@code null}
     */
    public static ParsedQuery parse(String rawQuery) {
        List<String> contentTerms = new ArrayList<>();
        List<String> pathTerms    = new ArrayList<>();

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
            // bare tokens are intentionally ignored here; if no qualifiers are
            // found, isQualified() will be false and the caller falls back to
            // the original plain search(rawQuery) behaviour.
        }

        return new ParsedQuery(rawQuery, contentTerms, pathTerms);
    }
}
