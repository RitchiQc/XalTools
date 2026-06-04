package me.serbob.xaltools.abilities;

import lombok.Getter;
import me.serbob.xaltools.api.permission.PermissionManager;
import me.serbob.xaltools.manager.BlacklistManager;
import me.serbob.commons.utils.nbt.NBTUtils;
import me.serbob.xaltools.utils.item.ItemUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractAbility implements Listener {
    @Getter private final String nbt;

    public AbstractAbility(String nbt) {
        this.nbt = nbt;
    }

    public boolean hasAbility(ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return false;

        return NBTUtils.getInstance().hasNbt(item, nbt);
    }

    public boolean isProtected(Player player, Location location) {
        if (BlacklistManager.getInstance().isBlacklistedWorld(location.getWorld()))
            return true;

        return PermissionManager.getInstance().isProtected(player, location);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handleBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInHand();

        if (!hasAbility(tool))
            return;

        if (isProtected(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }

        onBlockBreak(player, event, tool);

        if (event.isCancelled())
            return;

        if (!shouldSkipDefaultDurabilityReduction(player, event, tool)) {
            ItemUtil.reduceDurability(player, tool, 1);
        }
    }

    protected boolean shouldSkipDefaultDurabilityReduction(Player player, BlockBreakEvent event, ItemStack tool) {
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handleInteract(PlayerInteractEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInHand();

        if (tool == null || !hasAbility(tool))
            return;

        if (isProtected(player, player.getLocation())) {
            event.setCancelled(true);
            return;
        }

        onInteract(player, event, tool);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handleBucketFill(PlayerBucketFillEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInHand();

        if (!hasAbility(tool))
            return;

        if (isProtected(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }

        onBucketFill(player, event, tool);
    }

    protected abstract void onBlockBreak(Player player, BlockBreakEvent event, ItemStack tool);
    protected abstract void onInteract(Player player, PlayerInteractEvent event, ItemStack tool);
    protected abstract void onBucketFill(Player player, PlayerBucketFillEvent event, ItemStack tool);
}
