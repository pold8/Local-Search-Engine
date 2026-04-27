package org.example.ranking;

/**
 * Ranking strategy that orders results alphabetically by file name (A → Z).
 */
public class AlphabeticalRankingStrategy implements RankingStrategy {

    @Override
    public String getOrderByClause() {
        return "ORDER BY f.name ASC";
    }
}
