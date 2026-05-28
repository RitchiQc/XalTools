package me.serbob.xaltools.abilities.impl;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import me.serbob.xaltools.abilities.AbstractAbility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InfiniteFireworkAbility extends AbstractAbility {
    private final Map<UUID, Long> lastUse = new HashMap<>();
    private static final long COOLDOWN_MS = 200;

    public InfiniteFireworkAbility() {
        super("infinite_firework");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onElytraBoost(PlayerElytraBoostEvent event) {
        Player player = event.getPlayer();
        ItemStack firework = event.getItemStack();

        if (!hasAbility(firework)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (lastUse.containsKey(playerId)) {
            long timeSinceLastUse = currentTime - lastUse.get(playerId);
            if (timeSinceLastUse < COOLDOWN_MS) {
                event.setCancelled(true);
                return;
            }
        }

        lastUse.put(playerId, currentTime);

        event.setShouldConsume(false);
    }

    @Override
    public void onInteract(Player player, PlayerInteractEvent event, ItemStack tool) {
        if (tool == null || tool.getType() != Material.FIREWORK_ROCKET) {
            return;
        }

        if (player.isGliding() && event.getClickedBlock() != null) {
            Location playerLoc = player.getLocation();
            Location blockLoc = event.getClickedBlock().getLocation();

            if (playerLoc.distance(blockLoc) > 6.0) {
                event.setCancelled(true);
                return;
            }
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (lastUse.containsKey(playerId)) {
            long timeSinceLastUse = currentTime - lastUse.get(playerId);
            if (timeSinceLastUse < COOLDOWN_MS) {
                event.setCancelled(true);
                return;
            }
        }

        lastUse.put(playerId, currentTime);

        event.setCancelled(true);
    }

    @Override
    public void onBlockBreak(Player player, BlockBreakEvent event, ItemStack tool) {
        event.setCancelled(true);
    }

    @Override
    public void onBucketFill(Player player, PlayerBucketFillEvent event, ItemStack tool) {}
}
