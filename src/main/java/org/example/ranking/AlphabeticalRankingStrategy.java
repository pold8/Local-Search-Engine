package org.example.ranking;

public class AlphabeticalRankingStrategy implements RankingStrategy {

    @Override
    public String getOrderByClause() {
        return "ORDER BY f.name ASC";
    }
}
