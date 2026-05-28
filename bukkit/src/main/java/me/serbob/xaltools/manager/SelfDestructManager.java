package me.serbob.xaltools.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.serbob.commons.Commons;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.commons.utils.message.ChatUtil;
import me.serbob.commons.utils.nbt.NBTUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SelfDestructManager {
    @Getter(lazy = true)
    private static final SelfDestructManager instance = new SelfDestructManager();

    private static final Map<Character, Long> TIME_UNITS = new HashMap<>();

    static {
        TIME_UNITS.put('d', 86400000L);
        TIME_UNITS.put('h', 3600000L);
        TIME_UNITS.put('m', 60000L);
    }

    private long timerUpdateFrequency;
    private String selfDestructPrefix;
    private String timerPrefix;
    private Map<String, String> timeFormats = new HashMap<>();

    public void load() {
        FileConfiguration config = ConfigSelector.SELFDESTRUCT.getConfig();
        timerUpdateFrequency = config.getLong("timer_update_frequency", 20L);

        selfDestructPrefix = ChatUtil.c(config.getString("lore.self_destruct_prefix"));
        timerPrefix = ChatUtil.c(config.getString("lore.timer_prefix"));

        timeFormats.put("days", config.getString("time_format.days_format"));
        timeFormats.put("hours", config.getString("time_format.hours_format"));
        timeFormats.put("minutes", config.getString("time_format.minutes_format"));
        timeFormats.put("seconds", config.getString("time_format.seconds_format"));
        timeFormats.put("empty", config.getString("time_format.empty_format"));
    }

    public void initialize() {
        startTimerSystem();
    }

    private void startTimerSystem() {
        Commons.getFoliaLib().getScheduler().runTimerAsync(asyncTask -> processAllPlayersAsync(),
                20L, timerUpdateFrequency);
    }

    private void processAllPlayersAsync() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Commons.getFoliaLib().getScheduler().runLater(task -> {
                processPlayerInventory(player);
            }, 1L);
        }
    }

    public void processPlayerInventory(Player player) {
        Map<Integer, ItemUpdate> updates = new HashMap<>();

        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);

            if (item == null || item.getType() == Material.AIR) continue;

            // Migrate old items (secondsRemaining -> expirationTimestamp)
            if (NBTUtils.getInstance().hasSecondsRemaining(item)
                    && !NBTUtils.getInstance().hasExpirationTimestamp(item)) {
                migrateItemToTimestamp(item);
            }

            // Process items with expirationTimestamp (new system)
            if (NBTUtils.getInstance().hasExpirationTimestamp(item)) {
                long expiration = NBTUtils.getInstance().getExpirationTimestamp(item);
                long now = System.currentTimeMillis();
                long remainingMs = expiration - now;

                if (remainingMs <= 0) {
                    updates.put(slot, new ItemUpdate(true, 0));
                } else {
                    updates.put(slot, new ItemUpdate(false, remainingMs / 1000));
                }
            }
            // Process delayed items (old system, kept for compatibility)
            else if (NBTUtils.getInstance().hasSecondsRemaining(item)) {
                long secondsRemaining = NBTUtils.getInstance().getSecondsRemaining(item);
                boolean wasDelayed = secondsRemaining < 0;

                if (wasDelayed) {
                    secondsRemaining = Math.abs(secondsRemaining) - 1;
                    if (secondsRemaining <= 0) {
                        secondsRemaining = 1;
                    }
                    NBTUtils.getInstance().setSecondsRemaining(item, -secondsRemaining);
                } else {
                    secondsRemaining -= 1;
                }

                if (!wasDelayed && secondsRemaining <= 0) {
                    updates.put(slot, new ItemUpdate(true, 0));
                } else {
                    updates.put(slot, new ItemUpdate(false, wasDelayed ? -secondsRemaining : secondsRemaining));
                }
            }
        }

        if (!updates.isEmpty()) {
            applyUpdatesForPlayer(player, updates);
        }
    }

    private void migrateItemToTimestamp(ItemStack item) {
        long secondsRemaining = NBTUtils.getInstance().getSecondsRemaining(item);
        if (secondsRemaining < 0) {
            // Delayed items: keep old system
            return;
        }

        long expirationTimestamp = System.currentTimeMillis() + (secondsRemaining * 1000);
        NBTUtils.getInstance().setExpirationTimestamp(item, expirationTimestamp);
        NBTUtils.getInstance().removeSecondsRemaining(item);
    }

    private void applyUpdatesForPlayer(Player player, Map<Integer, ItemUpdate> updates) {
        for (Map.Entry<Integer, ItemUpdate> entry : updates.entrySet()) {
            int slot = entry.getKey();
            ItemUpdate update = entry.getValue();

            if (update.destroy) {
                player.getInventory().setItem(slot, null);
            } else {
                ItemStack item = player.getInventory().getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    // Update NBT only for delayed items (old system)
                    if (NBTUtils.getInstance().hasSecondsRemaining(item)
                            && !NBTUtils.getInstance().hasExpirationTimestamp(item)) {
                        NBTUtils.getInstance().setSecondsRemaining(item, update.secondsRemaining);
                    }
                    updateItemLore(item, Math.abs(update.secondsRemaining));
                }
            }
        }
    }

    public void updateItemLore(ItemStack item, long secondsRemaining) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        boolean found = false;
        String newTimerLine = timerPrefix + formatTimeRemaining(Math.abs(secondsRemaining) * 1000);
        for (int i = 0; i < lore.size() - 1; i++) {
            if (lore.get(i).equals(selfDestructPrefix) &&
                    i + 1 < lore.size() && lore.get(i + 1).startsWith(timerPrefix)) {
                if (lore.get(i + 1).equals(newTimerLine)) {
                    return; // No change, skip update to avoid animation
                }
                lore.set(i + 1, newTimerLine);
                found = true;
                break;
            }
        }

        if (!found) {
            lore.add(selfDestructPrefix);
            lore.add(newTimerLine);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private String formatTimeRemaining(long milliseconds) {
        if (milliseconds <= 0) return timeFormats.get("empty");

        long totalSeconds = milliseconds / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) result.append(String.format(timeFormats.get("days"), days));
        if (hours > 0) result.append(String.format(timeFormats.get("hours"), hours));
        if (minutes > 0) result.append(String.format(timeFormats.get("minutes"), minutes));
        // Only show seconds when less than 60 seconds remain to reduce item updates/animations
        if (totalSeconds < 60 && (seconds > 0 || result.length() == 0)) {
            result.append(String.format(timeFormats.get("seconds"), seconds));
        }

        return result.length() > 0 ? result.toString().trim() : timeFormats.get("empty");
    }

    public long parseTime(String timeStr) {
        if (timeStr == null || timeStr.length() < 2) return -1;

        try {
            char unit = timeStr.charAt(timeStr.length() - 1);
            int value = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
            Long multiplier = TIME_UNITS.get(unit);
            return multiplier != null ? multiplier * value : -1L;
        } catch (Exception e) {
            return -1;
        }
    }

    public void addTimedItem(Player player, ItemStack item, long destructTimeMillis, boolean delayed) {
        long seconds = destructTimeMillis / 1000;
        applyTimerToItem(item, seconds, delayed);
    }

    public void applyTimerToItem(ItemStack item, long seconds, boolean delayed) {
        if (delayed) {
            // Keep old system for delayed items
            NBTUtils.getInstance().setSecondsRemaining(item, -seconds);
        } else {
            long expirationTimestamp = System.currentTimeMillis() + (seconds * 1000);
            NBTUtils.getInstance().setExpirationTimestamp(item, expirationTimestamp);
        }
        updateItemLore(item, seconds);
    }

    public void onPlayerJoin(Player player) {
        processPlayerInventory(player);
    }

    private static class ItemUpdate {
        final boolean destroy;
        final long secondsRemaining;

        ItemUpdate(boolean destroy, long secondsRemaining) {
            this.destroy = destroy;
            this.secondsRemaining = secondsRemaining;
        }
    }
}
