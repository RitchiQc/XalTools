package me.serbob.xaltools.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.serbob.commons.Commons;
import me.serbob.xaltools.manager.database.DatabaseProvider;
import me.serbob.xaltools.manager.database.MySQLProvider;
import me.serbob.xaltools.manager.database.SQLiteProvider;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemTrackerManager {
    @Getter(lazy = true)
    private static final ItemTrackerManager instance = new ItemTrackerManager();

    private DatabaseProvider provider;

    public void initialize() {
        FileConfiguration config = Commons.getPluginInstance().getConfig();
        String storageType = config.getString("storage.type", "sqlite").toLowerCase();

        if (storageType.equals("mysql")) {
            provider = new MySQLProvider();
        } else {
            provider = new SQLiteProvider();
        }

        provider.initialize();
    }

    public void addItem(String toolName, UUID playerUuid, String itemUuid, Long expiresAt) {
        if (provider == null) return;
        provider.addItem(toolName, playerUuid, itemUuid, expiresAt);
    }

    public void removeItem(String itemUuid) {
        if (provider == null) return;
        provider.removeItem(itemUuid);
    }

    public void removeExpiredItems() {
        if (provider == null) return;
        provider.removeExpiredItems();
    }

    public Map<String, Integer> getStats() {
        if (provider == null) return Map.of();
        return provider.getStats();
    }

    public int getTotalCount() {
        if (provider == null) return 0;
        return provider.getTotalCount();
    }

    public void close() {
        if (provider != null) {
            provider.close();
        }
    }
}
