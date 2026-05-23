package org.example.query;

public class BaseQueryBuilder implements QueryBuilder {

    @Override
    public String build(String rawQuery) {
        return rawQuery == null ? "" : rawQuery;
    }
}
