package org.example.ranking;

public class RelevanceRankingStrategy implements RankingStrategy {

    @Override
    public String getOrderByClause() {
        return "ORDER BY (rank * -1 * f.path_score) DESC";
    }
}
