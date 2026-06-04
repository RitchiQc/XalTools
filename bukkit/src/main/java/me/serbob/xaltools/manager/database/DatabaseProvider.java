package me.serbob.xaltools.manager.database;

import java.util.Map;
import java.util.UUID;

public interface DatabaseProvider {
    void initialize();
    void close();

    void addItem(String toolName, UUID playerUuid, String itemUuid, Long expiresAt);
    void removeItem(String itemUuid);
    void removeExpiredItems();

    Map<String, Integer> getStats();
    int getTotalCount();
}
