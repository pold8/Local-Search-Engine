package org.example.ranking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RankingStrategyTest {

    @Test
    void testRelevanceRankingStrategy() {
        RankingStrategy strategy = new RelevanceRankingStrategy();
        assertEquals("ORDER BY (rank * -1 * f.path_score) DESC", strategy.getOrderByClause());
    }

    @Test
    void testDateRankingStrategy() {
        RankingStrategy strategy = new DateRankingStrategy();
        assertEquals("ORDER BY f.last_modified DESC", strategy.getOrderByClause());
    }

    @Test
    void testAlphabeticalRankingStrategy() {
        RankingStrategy strategy = new AlphabeticalRankingStrategy();
        assertEquals("ORDER BY f.name ASC", strategy.getOrderByClause());
    }
}
