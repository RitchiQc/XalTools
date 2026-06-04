package me.serbob.xaltools.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.serbob.commons.Commons;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.commons.utils.message.ChatUtil;
import me.serbob.commons.utils.nbt.NBTUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
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
    private String dateFormat;
    private Map<String, String> timeFormats = new HashMap<>();

    public void load() {
        FileConfiguration config = ConfigSelector.SELFDESTRUCT.getConfig();
        timerUpdateFrequency = config.getLong("timer_update_frequency", 20L);

        selfDestructPrefix = ChatUtil.c(config.getString("lore.self_destruct_prefix"));
        timerPrefix = ChatUtil.c(config.getString("lore.timer_prefix"));
        dateFormat = config.getString("lore.date_format", "dd/MM/yyyy HH:mm");

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
        
        // Scan world containers and entities every minute (1200 ticks)
        Commons.getFoliaLib().getScheduler().runTimer(task -> processWorldItems(),
                1200L, 1200L);
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
                player.getInventory().setItem(slot, item);
                item = player.getInventory().getItem(slot);
            }

            // Process items with expirationTimestamp (new system)
            if (NBTUtils.getInstance().hasExpirationTimestamp(item)) {
                long expiration = NBTUtils.getInstance().getExpirationTimestamp(item);
                long now = System.currentTimeMillis();
                long remainingMs = expiration - now;

                if (remainingMs <= 0) {
                    updates.put(slot, new ItemUpdate(true, 0));
                }
                // No lore update needed - date is static
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
                    player.getInventory().setItem(slot, item);
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
        updateItemLoreWithDate(item, expirationTimestamp);
    }

    private void applyUpdatesForPlayer(Player player, Map<Integer, ItemUpdate> updates) {
        for (Map.Entry<Integer, ItemUpdate> entry : updates.entrySet()) {
            int slot = entry.getKey();
            ItemUpdate update = entry.getValue();

            if (update.destroy) {
                ItemStack destroyedItem = player.getInventory().getItem(slot);
                if (destroyedItem != null) {
                    if (NBTUtils.getInstance().hasItemUuid(destroyedItem)) {
                        String itemUuid = NBTUtils.getInstance().getItemUuid(destroyedItem);
                        ItemTrackerManager.getInstance().removeItem(itemUuid);
                    }
                }
                player.getInventory().setItem(slot, null);
            } else {
                ItemStack item = player.getInventory().getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    // Update NBT and lore only for delayed items (old system)
                    if (NBTUtils.getInstance().hasSecondsRemaining(item)
                            && !NBTUtils.getInstance().hasExpirationTimestamp(item)) {
                        NBTUtils.getInstance().setSecondsRemaining(item, update.secondsRemaining);
                        updateItemLoreWithCountdown(item, Math.abs(update.secondsRemaining));
                        player.getInventory().setItem(slot, item);
                    }
                    // No lore update for new system - date is static
                }
            }
        }
    }

    public void updateItemLoreWithDate(ItemStack item, long expirationTimestamp) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        boolean found = false;
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        String dateStr = sdf.format(new Date(expirationTimestamp));
        String newTimerLine = timerPrefix + dateStr;
        
        for (int i = 0; i < lore.size() - 1; i++) {
            if (lore.get(i).equals(selfDestructPrefix) &&
                    i + 1 < lore.size() && lore.get(i + 1).startsWith(timerPrefix)) {
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

    public void updateItemLoreWithCountdown(ItemStack item, long secondsRemaining) {
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
            updateItemLoreWithCountdown(item, seconds);
        } else {
            long expirationTimestamp = System.currentTimeMillis() + (seconds * 1000);
            NBTUtils.getInstance().setExpirationTimestamp(item, expirationTimestamp);
            updateItemLoreWithDate(item, expirationTimestamp);
        }
        // Generate and store unique item UUID for tracking
        if (!NBTUtils.getInstance().hasItemUuid(item)) {
            NBTUtils.getInstance().setItemUuid(item, UUID.randomUUID().toString());
        }
    }

    public void onPlayerJoin(Player player) {
        processPlayerInventory(player);
    }

    // ==================== WORLD SCANNING ====================

    private void processWorldItems() {
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                // Process tile entities (chests, barrels, hoppers, furnaces, etc.)
                for (BlockState state : chunk.getTileEntities()) {
                    if (state instanceof InventoryHolder) {
                        processInventory(((InventoryHolder) state).getInventory());
                    }
                }
                
                // Process entities (item frames, armor stands, minecarts, etc.)
                for (Entity entity : chunk.getEntities()) {
                    if (entity instanceof Player) continue;
                    
                    if (entity instanceof ItemFrame) {
                        processItemFrame((ItemFrame) entity);
                    } else if (entity instanceof ArmorStand) {
                        processArmorStand((ArmorStand) entity);
                    } else if (entity instanceof InventoryHolder) {
                        processInventory(((InventoryHolder) entity).getInventory());
                    }
                }
            }
        }
    }

    private void processInventory(org.bukkit.inventory.Inventory inventory) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;

            // Migrate old items
            if (NBTUtils.getInstance().hasSecondsRemaining(item)
                    && !NBTUtils.getInstance().hasExpirationTimestamp(item)) {
                migrateItemToTimestamp(item);
                inventory.setItem(slot, item);
                item = inventory.getItem(slot);
            }

            boolean destroy = false;

            if (NBTUtils.getInstance().hasExpirationTimestamp(item)) {
                long expiration = NBTUtils.getInstance().getExpirationTimestamp(item);
                if (expiration <= System.currentTimeMillis()) {
                    destroy = true;
                }
            } else if (NBTUtils.getInstance().hasSecondsRemaining(item)) {
                long secondsRemaining = NBTUtils.getInstance().getSecondsRemaining(item);
                boolean wasDelayed = secondsRemaining < 0;

                if (wasDelayed) {
                    secondsRemaining = Math.abs(secondsRemaining) - 1;
                    if (secondsRemaining <= 0) {
                        secondsRemaining = 1;
                    }
                    NBTUtils.getInstance().setSecondsRemaining(item, -secondsRemaining);
                    inventory.setItem(slot, item);
                } else {
                    secondsRemaining -= 1;
                    if (secondsRemaining <= 0) {
                        destroy = true;
                    } else {
                        NBTUtils.getInstance().setSecondsRemaining(item, secondsRemaining);
                        inventory.setItem(slot, item);
                    }
                }
            }

            if (destroy) {
                if (NBTUtils.getInstance().hasItemUuid(item)) {
                    String itemUuid = NBTUtils.getInstance().getItemUuid(item);
                    ItemTrackerManager.getInstance().removeItem(itemUuid);
                }
                inventory.setItem(slot, null);
            }
        }
    }

    private void processItemFrame(ItemFrame frame) {
        ItemStack item = frame.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (processItemForDestruction(item)) {
            frame.setItem(null);
        } else {
            // Re-set to persist NBT changes
            frame.setItem(item);
        }
    }

    private void processArmorStand(ArmorStand stand) {
        // Main hand
        ItemStack mainHand = stand.getEquipment().getItemInMainHand();
        if (mainHand != null && mainHand.getType() != Material.AIR) {
            if (processItemForDestruction(mainHand)) {
                stand.getEquipment().setItemInMainHand(null);
            } else {
                stand.getEquipment().setItemInMainHand(mainHand);
            }
        }
        
        // Off hand
        ItemStack offHand = stand.getEquipment().getItemInOffHand();
        if (offHand != null && offHand.getType() != Material.AIR) {
            if (processItemForDestruction(offHand)) {
                stand.getEquipment().setItemInOffHand(null);
            } else {
                stand.getEquipment().setItemInOffHand(offHand);
            }
        }
        
        // Armor
        ItemStack[] armor = stand.getEquipment().getArmorContents();
        boolean modified = false;
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null && armor[i].getType() != Material.AIR) {
                if (processItemForDestruction(armor[i])) {
                    armor[i] = null;
                    modified = true;
                } else {
                    modified = true; // NBT may have changed
                }
            }
        }
        if (modified) {
            stand.getEquipment().setArmorContents(armor);
        }
    }

    /**
     * Processes an item for potential destruction. Handles migration and countdown.
     * Returns true if the item should be destroyed.
     */
    private boolean processItemForDestruction(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        // Migrate old items
        if (NBTUtils.getInstance().hasSecondsRemaining(item)
                && !NBTUtils.getInstance().hasExpirationTimestamp(item)) {
            migrateItemToTimestamp(item);
        }

        boolean destroy = false;

        if (NBTUtils.getInstance().hasExpirationTimestamp(item)) {
            long expiration = NBTUtils.getInstance().getExpirationTimestamp(item);
            if (expiration <= System.currentTimeMillis()) {
                destroy = true;
            }
        } else if (NBTUtils.getInstance().hasSecondsRemaining(item)) {
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
                if (secondsRemaining <= 0) {
                    destroy = true;
                } else {
                    NBTUtils.getInstance().setSecondsRemaining(item, secondsRemaining);
                }
            }
        }

        if (destroy) {
            if (NBTUtils.getInstance().hasItemUuid(item)) {
                String itemUuid = NBTUtils.getInstance().getItemUuid(item);
                ItemTrackerManager.getInstance().removeItem(itemUuid);
            }
        }

        return destroy;
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
