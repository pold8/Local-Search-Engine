package org.example.ranking;

/**
 * Strategy interface for controlling how search results are sorted.
 *
 * <p>Each implementation returns a complete SQL {@code ORDER BY} clause
 * (including the {@code ORDER BY} keyword) that is appended directly to
 * the dynamic query built in {@link org.example.db.FileRepository}.
 */
public interface RankingStrategy {

    /**
     * Returns the SQL {@code ORDER BY} clause to append to a search query.
     *
     * <p>The clause must be self-contained and valid within the join context
     * used by the repository (tables aliased as {@code f} for {@code files}
     * and {@code fts} for {@code files_fts}).
     *
     * @return a non-null SQL ORDER BY string, e.g. {@code "ORDER BY f.name ASC"}
     */
    String getOrderByClause();
}
