package me.serbob.xaltools.abilities.impl;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.serbob.commons.Commons;
import me.serbob.xaltools.abilities.AbstractAbility;
import me.serbob.xaltools.manager.BlacklistManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.RayTraceResult;

import java.util.*;

public class MultiToolAbility extends AbstractAbility implements Listener {
    private final Map<Player, WrappedTask> activeCheckers = new HashMap<>();

    private static final Set<Material> SWORD_MATERIALS = new HashSet<>();
    static {
        try {
            SWORD_MATERIALS.add(Material.valueOf("COBWEB"));
        } catch (Exception ignored) {}

        try {
            SWORD_MATERIALS.add(Material.valueOf("BAMBOO"));
            SWORD_MATERIALS.add(Material.valueOf("BAMBOO_SAPLING"));
        } catch (IllegalArgumentException ignored) {}
    }

    public MultiToolAbility() {
        super("amethyst_multitool");
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        if (newItem != null && hasAbility(newItem)) {
            startToolChecker(player);
        } else {
            stopToolChecker(player);
        }
    }

    private void startToolChecker(Player player) {
        stopToolChecker(player);

        class ToolCheckerData {
            Material lastMaterial = null;
        }

        ToolCheckerData data = new ToolCheckerData();

        WrappedTask task = Commons.getFoliaLib().getScheduler().runAtEntityTimer(player, () -> {
            ItemStack item = player.getInventory().getItemInHand();
            if (!hasAbility(item)) {
                stopToolChecker(player);
                return;
            }

            Material bestTool = null;

            RayTraceResult rayTrace = player.rayTraceBlocks(5);
            if (rayTrace != null && rayTrace.getHitEntity() != null) {
                Entity entity = rayTrace.getHitEntity();
                if (entity instanceof LivingEntity) {
                    bestTool = Material.NETHERITE_SWORD;
                }
            }

            if (bestTool == null) {
                for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                    if (entity instanceof LivingEntity && player.hasLineOfSight(entity)) {
                        Location playerEye = player.getEyeLocation();
                        Location entityLoc = entity.getLocation().add(0, entity.getHeight() / 2, 0);

                        if (playerEye.getDirection().normalize().dot(entityLoc.subtract(playerEye).toVector().normalize()) > 0.98) {
                            bestTool = Material.NETHERITE_SWORD;
                            break;
                        }
                    }
                }
            }

            if (bestTool == null) {
                Block targetBlock = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);
                if (targetBlock != null && targetBlock.getType() != Material.AIR) {
                    bestTool = getBestToolForBlock(targetBlock);
                }
            }

            if (bestTool != null && bestTool != data.lastMaterial) {
                data.lastMaterial = bestTool;
                updateToolMaterial(player, item, bestTool);
            }
        }, 1L, 2L);

        activeCheckers.put(player, task);
    }

    private void stopToolChecker(Player player) {
        WrappedTask task = activeCheckers.remove(player);
        if (task != null) {
            task.cancel();
        }
    }

    private Material getBestToolForBlock(Block block) {
        Material blockType = block.getType();

        if (SWORD_MATERIALS.contains(blockType)) {
            return Material.NETHERITE_SWORD;
        }

        ItemStack pickaxe = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
        ItemStack shovel = new ItemStack(Material.NETHERITE_SHOVEL);
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);

        float pickaxeSpeed = block.getDestroySpeed(pickaxe);
        float axeSpeed = block.getDestroySpeed(axe);
        float shovelSpeed = block.getDestroySpeed(shovel);
        float swordSpeed = block.getDestroySpeed(sword);

        float maxSpeed = Math.max(Math.max(pickaxeSpeed, axeSpeed), Math.max(shovelSpeed, swordSpeed));

        if (swordSpeed == maxSpeed && swordSpeed > 1.0f) {
            return Material.NETHERITE_SWORD;
        } else if (axeSpeed == maxSpeed && axeSpeed > pickaxeSpeed) {
            return Material.NETHERITE_AXE;
        } else if (shovelSpeed == maxSpeed && shovelSpeed > pickaxeSpeed) {
            return Material.NETHERITE_SHOVEL;
        } else {
            return Material.NETHERITE_PICKAXE;
        }
    }

    private void updateToolMaterial(Player player, ItemStack item, Material newMaterial) {
        if (item.getType() == newMaterial) return;

        ItemMeta meta = item.getItemMeta();
        item.setType(newMaterial);
        item.setItemMeta(meta);

        if (newMaterial == Material.NETHERITE_SWORD) {
            player.spawnParticle(Particle.ENCHANTED_HIT, player.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.02);
            player.spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
        } else {
            player.spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.3f, 1.5f);
    }

    @Override
    public void onBlockBreak(Player player, BlockBreakEvent event, ItemStack tool) {
        Block block = event.getBlock();

        if (BlacklistManager.getInstance().isBlacklistedBlock(block)) {
            event.setCancelled(true);
            return;
        }

        Location loc = block.getLocation().add(0.5, 0.5, 0.5);

        block.getWorld().spawnParticle(Particle.PORTAL, loc, 8, 0.1, 0.1, 0.1, 0.01);
        block.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc, 5, 0.15, 0.15, 0.15, 0.005);
        block.getWorld().spawnParticle(Particle.WITCH, loc, 3, 0.1, 0.1, 0.1, 0);
    }

    @Override
    public void onInteract(Player player, PlayerInteractEvent event, ItemStack tool) {}

    @Override
    public void onBucketFill(Player player, PlayerBucketFillEvent event, ItemStack tool) {}
}
