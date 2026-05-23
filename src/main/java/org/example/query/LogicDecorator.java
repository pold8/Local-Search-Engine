package org.example.query;

import java.util.Set;

public class LogicDecorator implements QueryBuilder {

    private static final Set<String> OPERATORS = Set.of("OR", "AND", "NOT");

    private final QueryBuilder wrapped;

    public LogicDecorator(QueryBuilder wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String build(String rawQuery) {
        String base = wrapped.build(rawQuery);
        String[] tokens = base.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) sb.append(" ");
            String t = tokens[i];
            if (OPERATORS.contains(t.toUpperCase())) {
                sb.append(t.toUpperCase());
            } else if (t.endsWith("*")) {
                sb.append(t); // already has wildcard
            } else {
                sb.append(t).append("*");
            }
        }
        return sb.toString();
    }
}
