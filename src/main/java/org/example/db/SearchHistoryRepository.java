package org.example.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchHistoryRepository {
    private final Connection connection;

    public SearchHistoryRepository(Connection connection) {
        this.connection = connection;
    }

    public void saveSearch(String query) {
        String sql = "INSERT INTO search_history (query, timestamp) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, query);
            stmt.setLong(2, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getSuggestions(String prefix) {
        List<String> suggestions = new ArrayList<>();
        String sql = "SELECT query, COUNT(*) as freq FROM search_history WHERE query LIKE ? GROUP BY query ORDER BY freq DESC LIMIT 3";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                suggestions.add(rs.getString("query"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suggestions;
    }

    public int getQueryFrequency(String query) {
        String sql = "SELECT COUNT(*) FROM search_history WHERE query = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
