package org.example.ranking;

/**
 * Ranking strategy that orders results by most recently modified file first.
 */
public class DateRankingStrategy implements RankingStrategy {

    @Override
    public String getOrderByClause() {
        return "ORDER BY f.last_modified DESC";
    }
}
