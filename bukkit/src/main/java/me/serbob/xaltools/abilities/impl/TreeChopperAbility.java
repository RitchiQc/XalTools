package me.serbob.xaltools.abilities.impl;

import me.serbob.commons.Commons;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.xaltools.abilities.AbstractAbility;
import me.serbob.xaltools.manager.BlacklistManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TreeChopperAbility extends AbstractAbility {

    private static final Set<Material> LOG_TYPES = new HashSet<>();
    private static final Set<Material> LEAF_TYPES = new HashSet<>();
    private static boolean initialized = false;

    public TreeChopperAbility() {
        super("amethyst_axe");
        loadMaterialsFromConfig();
    }

    private synchronized void loadMaterialsFromConfig() {
        if (initialized) return;

        FileConfiguration config = ConfigSelector.TREECHOPPER.getConfig();

        // Load log types from config, fallback to defaults
        List<String> logTypeNames = config.getStringList("log-types");
        if (logTypeNames != null && !logTypeNames.isEmpty()) {
            for (String name : logTypeNames) {
                try {
                    LOG_TYPES.add(Material.valueOf(name.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
        } else {
            // Default fallback
            for (Material material : Material.values()) {
                String materialName = material.name();
                if (materialName.endsWith("_LOG") || materialName.endsWith("_STEM")
                        || materialName.endsWith("_WOOD") || materialName.endsWith("_HYPHAE")
                        || materialName.equals("LOG") || materialName.equals("LOG_2")) {
                    LOG_TYPES.add(material);
                }
            }
        }

        // Load leaf types from config, fallback to defaults
        List<String> leafTypeNames = config.getStringList("leaf-types");
        if (leafTypeNames != null && !leafTypeNames.isEmpty()) {
            for (String name : leafTypeNames) {
                try {
                    LEAF_TYPES.add(Material.valueOf(name.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
        } else {
            // Default fallback
            for (Material material : Material.values()) {
                String materialName = material.name();
                if (materialName.endsWith("_LEAVES")
                        || materialName.equals("LEAVES") || materialName.equals("LEAVES_2")
                        || materialName.equals("NETHER_WART_BLOCK") || materialName.equals("WARPED_WART_BLOCK")
                        || materialName.equals("SHROOMLIGHT")) {
                    LEAF_TYPES.add(material);
                }
            }
        }

        initialized = true;
    }

    @Override
    public void onBlockBreak(Player player, BlockBreakEvent event, ItemStack tool) {
        Block startBlock = event.getBlock();

        if (this.isProtected(player, startBlock.getLocation()))
            return;

        if (!isLogBlock(startBlock)) {
            return;
        }

        boolean breakLeaves = ConfigSelector.TREECHOPPER.getConfig().getBoolean("break-leaves", false);
        Set<Block> treeBlocks = getTreeBlocks(startBlock, breakLeaves);
        Location centerLoc = startBlock.getLocation();

        try {
            player.playSound(centerLoc, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
        } catch (Exception e) {
            try {
                player.playSound(centerLoc, Sound.valueOf("LEVEL_UP"), 0.5f, 2.0f);
            } catch (Exception ignored) {}
        }

        boolean instantBreak = ConfigSelector.TREECHOPPER.getConfig().getBoolean("instant-break", false);
        boolean durabilityPerLog = ConfigSelector.TREECHOPPER.getConfig().getBoolean("durability-per-log", false);
        int breakDelay = ConfigSelector.TREECHOPPER.getConfig().getInt("break-delay", 2);
        if (breakDelay < 0) breakDelay = 2;

        // Sort blocks from bottom to top (lowest Y first) for visual effect
        List<Block> sortedBlocks = new ArrayList<>(treeBlocks);
        sortedBlocks.sort(Comparator.comparingInt(b -> b.getY()));

        int delay = 0;
        for (Block block : sortedBlocks) {
            if (this.isProtected(player, block.getLocation()))
                continue;

            if (BlacklistManager.getInstance().isBlacklistedBlock(block))
                continue;

            int currentDelay = instantBreak ? 0 : delay;
            Commons.getFoliaLib().getScheduler().runAtLocationLater(block.getLocation(), () -> {
                breakBlock(block, tool);
            }, currentDelay);

            if (!instantBreak)
                delay += breakDelay;
        }

        if (durabilityPerLog) {
            int totalLogs = treeBlocks.size();
            me.serbob.xaltools.utils.item.ItemUtil.reduceDurability(player, tool, totalLogs - 1);
        }
    }

    @Override
    protected boolean shouldSkipDefaultDurabilityReduction(Player player, BlockBreakEvent event, ItemStack tool) {
        boolean durabilityPerLog = ConfigSelector.TREECHOPPER.getConfig().getBoolean("durability-per-log", false);
        return durabilityPerLog;
    }

    @Override
    public void onInteract(Player player, PlayerInteractEvent event, ItemStack tool) {}

    @Override
    public void onBucketFill(Player player, PlayerBucketFillEvent event, ItemStack tool) {}

    private void breakBlock(Block block, ItemStack tool) {
        Location particleLoc = block.getLocation().add(0.5, 0.5, 0.5);

        try {
            block.getWorld().spawnParticle(Particle.PORTAL,
                    particleLoc, 10, 0.2, 0.2, 0.2, 0.02);
        } catch (Exception ignored) {}

        try {
            block.getWorld().spawnParticle(Particle.REVERSE_PORTAL,
                    particleLoc, 8, 0.2, 0.2, 0.2, 0.01);
        } catch (Exception ignored) {}

        try {
            block.getWorld().spawnParticle(Particle.WITCH,
                    particleLoc, 5, 0.15, 0.15, 0.15, 0);
        } catch (Exception e) {
            try {
                block.getWorld().spawnParticle(Particle.valueOf("WITCH_MAGIC"),
                        particleLoc, 5, 0.15, 0.15, 0.15, 0);
            } catch (Exception ignored) {}
        }

        if (isLeafBlock(block)) {
            try {
                block.getWorld().playSound(particleLoc, Sound.BLOCK_GRASS_BREAK, 0.7f, 1.1f);
            } catch (Exception e) {
                try {
                    block.getWorld().playSound(particleLoc, Sound.valueOf("DIG_GRASS"), 0.7f, 1.1f);
                } catch (Exception ignored) {}
            }
            block.breakNaturally();
        } else {
            try {
                block.getWorld().playSound(particleLoc, Sound.BLOCK_WOOD_BREAK, 0.7f, 1.1f);
            } catch (Exception e) {
                try {
                    block.getWorld().playSound(particleLoc, Sound.valueOf("DIG_WOOD"), 0.7f, 1.1f);
                } catch (Exception ignored) {}
            }
            block.breakNaturally(tool);
        }
    }

    private boolean isLogBlock(Block block) {
        return LOG_TYPES.contains(block.getType());
    }

    private boolean isLeafBlock(Block block) {
        return LEAF_TYPES.contains(block.getType());
    }

    private boolean isTreeBlock(Block block) {
        return isLogBlock(block) || isLeafBlock(block);
    }

    private Set<Block> getTreeBlocks(Block startBlock, boolean includeLeaves) {
        Set<Block> treeBlocks = new HashSet<>();
        Queue<Block> toCheck = new LinkedList<>();
        toCheck.add(startBlock);
        treeBlocks.add(startBlock);

        int maxLogs = ConfigSelector.TREECHOPPER.getConfig().getInt("max-logs", 300);
        if (maxLogs <= 0) {
            maxLogs = 300;
        }
        int maxLeaves = ConfigSelector.TREECHOPPER.getConfig().getInt("max-leaves", 300);
        if (maxLeaves <= 0) {
            maxLeaves = 300;
        }

        int logCount = 1;
        int leafCount = 0;

        while (!toCheck.isEmpty() && treeBlocks.size() < (maxLogs + maxLeaves)) {
            Block current = toCheck.poll();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block relative = current.getRelative(x, y, z);
                        if (treeBlocks.contains(relative)) continue;

                        if (isLogBlock(relative)) {
                            if (logCount >= maxLogs) continue;
                            treeBlocks.add(relative);
                            toCheck.add(relative);
                            logCount++;
                        } else if (includeLeaves && isLeafBlock(relative)) {
                            if (leafCount >= maxLeaves) continue;
                            treeBlocks.add(relative);
                            toCheck.add(relative);
                            leafCount++;
                        }
                    }
                }
            }
        }

        return treeBlocks;
    }
}
