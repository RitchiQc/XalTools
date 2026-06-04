package me.serbob.xaltools.abilities.impl;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.serbob.commons.Commons;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.commons.enums.Messages;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
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

        if (newItem != null && hasAbility(newItem) && isEnabled(newItem)) {
            startMagnetTask(player);
        } else {
            stopMagnetTask(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractLowest(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (tool == null || !hasAbility(tool))
            return;

        // Prevent double-processing if AbstractAbility already handled it
        if (event.isCancelled())
            return;

        event.setCancelled(true);

        boolean enabled = !isEnabled(tool);
        setEnabled(tool, enabled);

        if (enabled) {
            startMagnetTask(player);
            Messages.MAGNET_ENABLED.sendBoth(player);
        } else {
            stopMagnetTask(player);
            Messages.MAGNET_DISABLED.sendBoth(player);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, enabled ? 1.5f : 0.8f);
    }

    @Override
    public void onInteract(Player player, PlayerInteractEvent event, ItemStack tool) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);

        boolean enabled = !isEnabled(tool);
        setEnabled(tool, enabled);

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
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!hasAbility(item) || !isEnabled(item)) {
                stopMagnetTask(player);
                return;
            }

            for (Item droppedItem : player.getWorld().getEntitiesByClass(Item.class)) {
                if (droppedItem.getLocation().distance(player.getLocation()) > range) {
                    continue;
                }
                if (droppedItem.isDead() || !droppedItem.isValid()) {
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

    @Override
    public void onBlockBreak(Player player, BlockBreakEvent event, ItemStack tool) {}

    @Override
    public void onBucketFill(Player player, PlayerBucketFillEvent event, ItemStack tool) {}
}
