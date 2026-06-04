package me.serbob.xaltools.manager.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.serbob.commons.Commons;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MySQLProvider implements DatabaseProvider {
    private HikariDataSource dataSource;

    @Override
    public void initialize() {
        try {
            FileConfiguration config = Commons.getPluginInstance().getConfig();
            String host = config.getString("storage.mysql.host", "localhost");
            int port = config.getInt("storage.mysql.port", 3306);
            String database = config.getString("storage.mysql.database", "xaltools");
            String username = config.getString("storage.mysql.username", "root");
            String password = config.getString("storage.mysql.password", "");
            int poolSize = config.getInt("storage.mysql.pool-size", 10);

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC");
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setMaximumPoolSize(poolSize);
            hikariConfig.setPoolName("XalTools-MySQL");
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(hikariConfig);

            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS tracked_items (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        tool_name VARCHAR(64) NOT NULL,
                        player_uuid VARCHAR(36),
                        item_uuid VARCHAR(36) UNIQUE NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        expires_at TIMESTAMP NULL
                    )
                """);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("[XalTools] Failed to initialize MySQL database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public void addItem(String toolName, UUID playerUuid, String itemUuid, Long expiresAt) {
        if (dataSource == null) return;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
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
        if (dataSource == null) return;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM tracked_items WHERE item_uuid = ?")) {
            ps.setString(1, itemUuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[XalTools] Failed to remove tracked item: " + e.getMessage());
        }
    }

    @Override
    public void removeExpiredItems() {
        if (dataSource == null) return;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
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
        if (dataSource == null) return stats;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
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
        if (dataSource == null) return 0;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
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
