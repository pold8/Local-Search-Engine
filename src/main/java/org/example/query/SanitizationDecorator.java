package org.example.query;

public class SanitizationDecorator implements QueryBuilder {

    // Characters that break FTS5 query syntax
    private static final String STRIP_PATTERN = "[\"'(){}\\[\\];^~|&!@#$%]";

    private final QueryBuilder wrapped;

    public SanitizationDecorator(QueryBuilder wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String build(String rawQuery) {
        String base = wrapped.build(rawQuery);
        return base.replaceAll(STRIP_PATTERN, "").replaceAll("\\s{2,}", " ").trim();
    }
}
