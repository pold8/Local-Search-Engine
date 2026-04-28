package org.example.db;

import org.example.model.DuplicateGroup;
import org.example.model.FileRecord;
import org.example.model.SearchResult;
import org.example.parser.ParsedQuery;
import org.example.ranking.RankingStrategy;
import org.example.ranking.RelevanceRankingStrategy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileRepository {

    private final Connection connection;
    private RankingStrategy rankingStrategy;

    public FileRepository(Connection connection) {
        this.connection = connection;
        this.rankingStrategy = new RelevanceRankingStrategy();
    }

    public void setRankingStrategy(RankingStrategy strategy) {
        this.rankingStrategy = strategy;
    }

    public void save(FileRecord record) throws SQLException {
        String insertFile = """
                INSERT INTO files (path, name, extension, size, last_modified, file_hash, preview, path_score)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String insertFts = """
                INSERT INTO files_fts (path, content)
                VALUES (?, ?)
                """;

        try (PreparedStatement stmtFile = connection.prepareStatement(insertFile);
                PreparedStatement stmtFts = connection.prepareStatement(insertFts)) {

            stmtFile.setString(1, record.getPath());
            stmtFile.setString(2, record.getName());
            stmtFile.setString(3, record.getExtension());
            stmtFile.setLong(4, record.getSize());
            stmtFile.setLong(5, record.getLastModified());
            stmtFile.setString(6, record.getFileHash());
            stmtFile.setString(7, record.getPreview());
            stmtFile.setDouble(8, record.getPathScore());
            stmtFile.executeUpdate();

            stmtFts.setString(1, record.getPath());
            stmtFts.setString(2, record.getContent());
            stmtFts.executeUpdate();
        }
    }

    public void update(FileRecord record) throws SQLException {
        String updateFile = """
                UPDATE files
                SET name = ?, extension = ?, size = ?, last_modified = ?, file_hash = ?, preview = ?, path_score = ?
                WHERE path = ?
                """;

        String deleteFts = "DELETE FROM files_fts WHERE path = ?";
        String insertFts = "INSERT INTO files_fts (path, content) VALUES (?, ?)";

        try (PreparedStatement stmtFile = connection.prepareStatement(updateFile);
                PreparedStatement stmtDeleteFts = connection.prepareStatement(deleteFts);
                PreparedStatement stmtInsertFts = connection.prepareStatement(insertFts)) {

            stmtFile.setString(1, record.getName());
            stmtFile.setString(2, record.getExtension());
            stmtFile.setLong(3, record.getSize());
            stmtFile.setLong(4, record.getLastModified());
            stmtFile.setString(5, record.getFileHash());
            stmtFile.setString(6, record.getPreview());
            stmtFile.setDouble(7, record.getPathScore());
            stmtFile.setString(8, record.getPath());
            stmtFile.executeUpdate();

            stmtDeleteFts.setString(1, record.getPath());
            stmtDeleteFts.executeUpdate();

            stmtInsertFts.setString(1, record.getPath());
            stmtInsertFts.setString(2, record.getContent());
            stmtInsertFts.executeUpdate();
        }
    }

    public void delete(String path) throws SQLException {
        try (PreparedStatement stmtFile = connection.prepareStatement(
                "DELETE FROM files WHERE path = ?");
                PreparedStatement stmtFts = connection.prepareStatement(
                        "DELETE FROM files_fts WHERE path = ?")) {

            stmtFile.setString(1, path);
            stmtFile.executeUpdate();

            stmtFts.setString(1, path);
            stmtFts.executeUpdate();
        }
    }

    public FileRecord findByPath(String path) throws SQLException {
        String sql = "SELECT * FROM files WHERE path = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, path);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }
            return null;
        }
    }

    public List<SearchResult> search(String query) throws SQLException {
        List<SearchResult> results = new ArrayList<>();

        String ftsSQL = """
                SELECT f.path, f.name, f.extension, f.size, f.last_modified, f.file_hash, f.preview, f.path_score
                FROM files_fts fts
                JOIN files f ON fts.path = f.path
                WHERE files_fts MATCH ?
                """
                + rankingStrategy.getOrderByClause() + """

                        LIMIT 20
                        """;

        String nameSQL = """
                SELECT path, name, extension, size, last_modified, file_hash, preview, path_score
                FROM files
                WHERE name LIKE ?
                LIMIT 10
                """;

        try (PreparedStatement stmtFts = connection.prepareStatement(ftsSQL);
                PreparedStatement stmtName = connection.prepareStatement(nameSQL)) {

            stmtFts.setString(1, sanitizeFtsQuery(query));
            ResultSet rsFts = stmtFts.executeQuery();
            int rank = 1;
            while (rsFts.next()) {
                FileRecord record = mapResultSet(rsFts);
                results.add(new SearchResult(record, record.getPreview(), rank++, "content match"));
            }

            stmtName.setString(1, "%" + query + "%");
            ResultSet rsName = stmtName.executeQuery();
            while (rsName.next()) {
                String path = rsName.getString("path");
                boolean alreadyFound = results.stream()
                        .anyMatch(r -> r.getFileRecord().getPath().equals(path));
                if (!alreadyFound) {
                    FileRecord record = mapResultSet(rsName);
                    results.add(new SearchResult(record, record.getPreview(), rank++, "filename match"));
                }
            }
        }

        return results;
    }

    public List<SearchResult> searchParsed(ParsedQuery query) throws SQLException {
        if (!query.isQualified()) {
            return search(query.getRawQuery());
        }

        List<SearchResult> results = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                SELECT f.path, f.name, f.extension, f.size,
                       f.last_modified, f.file_hash, f.preview, f.path_score
                FROM files f
                """);

        List<String> contentTerms = query.getContentTerms();
        List<String> pathTerms = query.getPathTerms();

        boolean hasFts = !contentTerms.isEmpty();
        boolean hasPath = !pathTerms.isEmpty();

        if (hasFts) {
            sql.append("JOIN files_fts fts ON fts.path = f.path ");
        }

        sql.append("WHERE 1=1 ");

        if (hasFts) {
            sql.append("AND files_fts MATCH ? ");
        }

        for (int i = 0; i < pathTerms.size(); i++) {
            sql.append("AND f.path LIKE ? ");
        }

        if (hasFts) {
            sql.append(rankingStrategy.getOrderByClause()).append(" ");
        }

        sql.append("LIMIT 20");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            int idx = 1;

            if (hasFts) {
                StringBuilder ftsExpr = new StringBuilder();
                for (int i = 0; i < contentTerms.size(); i++) {
                    if (i > 0)
                        ftsExpr.append(" AND ");
                    ftsExpr.append("\"").append(
                            contentTerms.get(i).replace("\"", "\"\"")).append("\"");
                }
                stmt.setString(idx++, ftsExpr.toString());
            }

            for (String pathTerm : pathTerms) {
                stmt.setString(idx++, "%" + pathTerm + "%");
            }

            ResultSet rs = stmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                FileRecord record = mapResultSet(rs);
                String matchReason = buildMatchReason(contentTerms, pathTerms);
                results.add(new SearchResult(record, record.getPreview(), rank++, matchReason));
            }
        }

        return results;
    }

    private String buildMatchReason(List<String> contentTerms, List<String> pathTerms) {
        StringBuilder sb = new StringBuilder();
        if (!contentTerms.isEmpty()) {
            sb.append("content match (").append(String.join(" AND ", contentTerms)).append(")");
        }
        if (!pathTerms.isEmpty()) {
            if (sb.length() > 0)
                sb.append("; ");
            sb.append("path match (").append(String.join(" AND ", pathTerms)).append(")");
        }
        return sb.toString();
    }

    public List<DuplicateGroup> findDuplicates() throws SQLException {
        List<DuplicateGroup> duplicates = new ArrayList<>();
        String sql = """
                SELECT file_hash, size, GROUP_CONCAT(path, '||') as paths, COUNT(*) as count
                FROM files
                WHERE file_hash IS NOT NULL AND file_hash != ''
                GROUP BY file_hash
                HAVING COUNT(*) > 1
                ORDER BY count DESC, size DESC
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String hash = rs.getString("file_hash");
                long size = rs.getLong("size");
                String pathsStr = rs.getString("paths");
                List<String> paths = List.of(pathsStr.split("\\|\\|"));
                duplicates.add(new DuplicateGroup(hash, size, paths));
            }
        }
        return duplicates;
    }

    public List<String> findDuplicatesOf(String path) throws SQLException {
        String hashSql = "SELECT file_hash FROM files WHERE path = ?";
        String hash = null;
        try (PreparedStatement stmt = connection.prepareStatement(hashSql)) {
            stmt.setString(1, path);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                hash = rs.getString("file_hash");
            }
        }

        if (hash == null || hash.isEmpty()) {
            return new ArrayList<>();
        }

        String dupSql = "SELECT path FROM files WHERE file_hash = ? AND path != ?";
        List<String> dups = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(dupSql)) {
            stmt.setString(1, hash);
            stmt.setString(2, path);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dups.add(rs.getString("path"));
            }
        }
        return dups;
    }

    private String sanitizeFtsQuery(String query) {
        String[] terms = query.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < terms.length; i++) {
            if (i > 0)
                sb.append(" ");
            sb.append("\"").append(terms[i].replace("\"", "\"\"")).append("\"");
        }
        return sb.toString();
    }

    private FileRecord mapResultSet(ResultSet rs) throws SQLException {
        FileRecord record = new FileRecord(
                rs.getString("path"),
                rs.getString("name"),
                rs.getString("extension"),
                rs.getLong("size"),
                rs.getLong("last_modified"),
                rs.getString("file_hash"),
                "",
                rs.getString("preview"));
        try {
            record.setPathScore(rs.getDouble("path_score"));
        } catch (SQLException ignored) {

        }
        return record;
    }
}
