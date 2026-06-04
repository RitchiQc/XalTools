package me.serbob.xaltools.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.serbob.commons.Commons;
import me.serbob.xaltools.tools.Tools;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemTrackerManager {
    @Getter(lazy = true)
    private static final ItemTrackerManager instance = new ItemTrackerManager();

    private Connection connection;

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
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        expires_at TIMESTAMP NULL
                    )
                """);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("[XalTools] Failed to initialize item tracker database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addItem(String toolName, UUID playerUuid, Long expiresAt) {
        if (connection == null) return;

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO tracked_items (tool_name, player_uuid, expires_at) VALUES (?, ?, ?)")) {
            ps.setString(1, toolName.toLowerCase());
            ps.setString(2, playerUuid != null ? playerUuid.toString() : null);
            if (expiresAt != null && expiresAt > 0) {
                ps.setTimestamp(3, new Timestamp(expiresAt));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[XalTools] Failed to add tracked item: " + e.getMessage());
        }
    }

    public void addItem(String toolName, UUID playerUuid) {
        addItem(toolName, playerUuid, null);
    }

    public void removeItem(String toolName, UUID playerUuid) {
        if (connection == null) return;

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM tracked_items WHERE tool_name = ? AND player_uuid = ? LIMIT 1")) {
            ps.setString(1, toolName.toLowerCase());
            ps.setString(2, playerUuid != null ? playerUuid.toString() : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[XalTools] Failed to remove tracked item: " + e.getMessage());
        }
    }

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

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                Bukkit.getLogger().warning("[XalTools] Failed to close database: " + e.getMessage());
            }
        }
    }
}
