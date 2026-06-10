package me.serbob.xaltools.abilities.impl;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.serbob.commons.Commons;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.commons.enums.Messages;
import me.serbob.commons.utils.message.ChatUtil;
import me.serbob.commons.utils.nbt.NBTUtils;
import me.serbob.xaltools.abilities.AbstractAbility;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagnetAbility extends AbstractAbility implements Listener {
    private final Map<Player, WrappedTask> activeTasks = new HashMap<>();
    private final double range;
    private final double speed;

    public MagnetAbility() {
        super("amethyst_magnet");
        this.range = ConfigSelector.MAGNET.getConfig().getDouble("range", 8.0);
        this.speed = ConfigSelector.MAGNET.getConfig().getDouble("speed", 0.15);
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if ((newItem != null && hasAbility(newItem) && isEnabled(newItem))
                || (hasAbility(offHand) && isEnabled(offHand))) {
            startMagnetTask(player);
        } else {
            stopMagnetTask(player);
        }
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = event.getMainHandItem();
        ItemStack offHand = event.getOffHandItem();

        boolean hasActiveMagnet = (mainHand != null && hasAbility(mainHand) && isEnabled(mainHand))
                || (offHand != null && hasAbility(offHand) && isEnabled(offHand));

        if (hasActiveMagnet) {
            startMagnetTask(player);
        } else {
            stopMagnetTask(player);
        }
    }

    @Override
    public void onInteract(Player player, PlayerInteractEvent event, ItemStack tool) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);

        EquipmentSlot hand = event.getHand() != null ? event.getHand() : EquipmentSlot.HAND;

        if (player.isSneaking()) {
            changeMode(player, hand, tool);
        } else {
            toggleMagnet(player, hand, tool);
        }
    }

    @Override
    protected void onAirInteract(Player player, EquipmentSlot hand, ItemStack tool) {
        if (player.isSneaking()) {
            changeMode(player, hand, tool);
        } else {
            toggleMagnet(player, hand, tool);
        }
    }

    private void changeMode(Player player, EquipmentSlot hand, ItemStack tool) {
        MagnetMode currentMode = getMode(tool);
        MagnetMode newMode = currentMode.next();
        setMode(tool, newMode);

        if (hand == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(tool);
        } else {
            player.getInventory().setItemInMainHand(tool);
        }

        if (newMode == MagnetMode.NORMAL) {
            Messages.MAGNET_MODE_NORMAL.sendBoth(player);
        } else {
            Messages.MAGNET_MODE_FARM.sendBoth(player);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
    }

    private void toggleMagnet(Player player, EquipmentSlot hand, ItemStack tool) {
        boolean enabled = !isEnabled(tool);
        setEnabled(tool, enabled);

        if (hand == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(tool);
        } else {
            player.getInventory().setItemInMainHand(tool);
        }

        if (enabled) {
            startMagnetTask(player);
            Messages.MAGNET_ENABLED.sendBoth(player);
        } else {
            stopMagnetTask(player);
            Messages.MAGNET_DISABLED.sendBoth(player);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, enabled ? 1.5f : 0.8f);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item != null && hasAbility(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item != null && hasAbility(item)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void startMagnetTask(Player player) {
        stopMagnetTask(player);

        WrappedTask task = Commons.getFoliaLib().getScheduler().runAtEntityTimer(player, () -> {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            boolean hasActiveMagnet = (hasAbility(mainHand) && isEnabled(mainHand))
                    || (hasAbility(offHand) && isEnabled(offHand));
            if (!hasActiveMagnet) {
                stopMagnetTask(player);
                return;
            }

            MagnetMode mode = MagnetMode.NORMAL;
            if (hasAbility(mainHand) && isEnabled(mainHand)) {
                mode = getMode(mainHand);
            } else if (hasAbility(offHand) && isEnabled(offHand)) {
                mode = getMode(offHand);
            }

            for (Item droppedItem : player.getWorld().getEntitiesByClass(Item.class)) {
                if (droppedItem.getLocation().distance(player.getLocation()) > range) {
                    continue;
                }
                if (droppedItem.isDead() || !droppedItem.isValid()) {
                    continue;
                }

                ItemStack itemStack = droppedItem.getItemStack();
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    continue;
                }

                if (!mode.isAllowed(itemStack.getType())) {
                    continue;
                }

                Vector direction = player.getLocation().toVector().subtract(droppedItem.getLocation().toVector()).normalize();
                droppedItem.setVelocity(direction.multiply(speed));

                drawParticleLine(droppedItem.getLocation().add(0, 0.3, 0), player.getLocation().add(0, 1, 0));
            }
        }, 1L, 2L);

        activeTasks.put(player, task);
    }

    private void stopMagnetTask(Player player) {
        WrappedTask task = activeTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
    }

    private void drawParticleLine(Location from, Location to) {
        Vector vector = to.toVector().subtract(from.toVector());
        double length = vector.length();
        vector = vector.normalize();

        double step = 0.3;
        for (double d = 0; d < length; d += step) {
            Location point = from.clone().add(vector.clone().multiply(d));
            from.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.fromRGB(255, 100, 0), 0.8f));
        }
    }

    private boolean isEnabled(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        return NBTUtils.getInstance().getBoolean(item, "magnet_enabled");
    }

    private void setEnabled(ItemStack item, boolean enabled) {
        NBTUtils.getInstance().setBoolean(item, "magnet_enabled", enabled);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setEnchantmentGlintOverride(enabled);
            item.setItemMeta(meta);
        }
    }

    private MagnetMode getMode(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return MagnetMode.NORMAL;
        }
        String modeName = NBTUtils.getInstance().getString(item, "magnet_mode");
        if (modeName == null || modeName.isEmpty()) {
            return MagnetMode.NORMAL;
        }
        try {
            return MagnetMode.valueOf(modeName);
        } catch (IllegalArgumentException e) {
            return MagnetMode.NORMAL;
        }
    }

    private void setMode(ItemStack item, MagnetMode mode) {
        NBTUtils.getInstance().setString(item, "magnet_mode", mode.name());
        updateLore(item, mode);
    }

    private void updateLore(ItemStack item, MagnetMode mode) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        List<String> lore = meta.getLore();
        if (lore == null) {
            return;
        }

        String modePrefix = "Mode: ";
        String newModeLine = ChatUtil.c(modePrefix + mode.getDisplayName());
        boolean found = false;

        for (int i = 0; i < lore.size(); i++) {
            String line = ChatUtil.c(lore.get(i));
            if (line.contains(ChatUtil.c(modePrefix))) {
                lore.set(i, newModeLine);
                found = true;
                break;
            }
        }

        if (!found) {
            lore.add(newModeLine);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    @Override
    public void onBlockBreak(Player player, BlockBreakEvent event, ItemStack tool) {}

    @Override
    protected boolean shouldSkipDefaultDurabilityReduction(Player player, BlockBreakEvent event, ItemStack tool) {
        return true;
    }

    @Override
    public void onBucketFill(Player player, PlayerBucketFillEvent event, ItemStack tool) {}
}
