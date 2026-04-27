package org.example.ranking;

/**
 * Default ranking strategy: combines FTS relevance with path quality.
 *
 * <p>SQLite FTS5 {@code rank} is negative — a lower (more negative) value
 * means a better text match. Multiplying by {@code -1} flips it to positive,
 * then multiplying by {@code f.path_score} (a value in [0, 1]) gives a
 * composite score where higher = better. Sorting {@code DESC} brings the
 * best matches to the top.
 */
public class RelevanceRankingStrategy implements RankingStrategy {

    @Override
    public String getOrderByClause() {
        return "ORDER BY (rank * -1 * f.path_score) DESC";
    }
}
