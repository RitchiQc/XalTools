package me.serbob.xaltools.manager.database;

import me.serbob.commons.Commons;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SQLiteProvider implements DatabaseProvider {
    private Connection connection;

    @Override
    public void initialize() {
        try {
            File dataFolder = Commons.getPluginInstance().getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "items.db").getAbsolutePath());

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS tracked_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        tool_name TEXT NOT NULL,
                        player_uuid TEXT,
                        item_uuid TEXT UNIQUE NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        expires_at TIMESTAMP NULL
                    )
                """);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("[XalTools] Failed to initialize SQLite database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("[XalTools] Failed to close SQLite database: " + e.getMessage());
            }
        }
    }

    @Override
    public void addItem(String toolName, UUID playerUuid, String itemUuid, Long expiresAt) {
        if (connection == null) return;

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO tracked_items (tool_name, player_uuid, item_uuid, expires_at) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, toolName.toLowerCase());
            ps.setString(2, playerUuid != null ? playerUuid.toString() : null);
            ps.setString(3, itemUuid);
            if (expiresAt != null && expiresAt > 0) {
                ps.setTimestamp(4, new Timestamp(expiresAt));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[XalTools] Failed to add tracked item: " + e.getMessage());
        }
    }

    @Override
    public void removeItem(String itemUuid) {
        if (connection == null) return;

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM tracked_items WHERE item_uuid = ?")) {
            ps.setString(1, itemUuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[XalTools] Failed to remove tracked item: " + e.getMessage());
        }
    }

    @Override
    public void removeExpiredItems() {
        if (connection == null) return;

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM tracked_items WHERE expires_at IS NOT NULL AND expires_at <= ?")) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[XalTools] Failed to remove expired items: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        if (connection == null) return stats;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT tool_name, COUNT(*) as count FROM tracked_items GROUP BY tool_name")) {
            while (rs.next()) {
                stats.put(rs.getString("tool_name"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[XalTools] Failed to get stats: " + e.getMessage());
        }

        return stats;
    }

    @Override
    public int getTotalCount() {
        if (connection == null) return 0;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM tracked_items")) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[XalTools] Failed to get total count: " + e.getMessage());
        }

        return 0;
    }
}
