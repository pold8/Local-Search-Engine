package org.example.ranking;

public class DateRankingStrategy implements RankingStrategy {

    @Override
    public String getOrderByClause() {
        return "ORDER BY f.last_modified DESC";
    }
}
