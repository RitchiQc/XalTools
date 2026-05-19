package me.serbob.mythictools.abilities.impl;

import me.serbob.commons.Commons;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.mythictools.abilities.AbstractAbility;
import me.serbob.mythictools.manager.BlacklistManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class TreeChopperAbility extends AbstractAbility {

    private static final Set<Material> LOG_TYPES = new HashSet<>();
    private static final Set<Material> LEAF_TYPES = new HashSet<>();

    static {
        for (Material material : Material.values()) {
            String name = material.name();
            if (isLogMaterial(name)) {
                LOG_TYPES.add(material);
            } else if (isLeafMaterial(name)) {
                LEAF_TYPES.add(material);
            }
        }
    }

    private static boolean isLogMaterial(
            String materialName
    ) {
        if (materialName.endsWith("_LOG") || materialName.endsWith("_STEM"))
            return true;

        return materialName.equals("LOG") || materialName.equals("LOG_2");
    }

    private static boolean isLeafMaterial(
            String materialName
    ) {
        if (materialName.endsWith("_LEAVES"))
            return true;

        if (materialName.equals("LEAVES") || materialName.equals("LEAVES_2"))
            return true;

        if (materialName.equals("NETHER_WART_BLOCK") || materialName.equals("WARPED_WART_BLOCK"))
            return true;

        return materialName.equals("SHROOMLIGHT");
    }

    public TreeChopperAbility() {
        super("amethyst_axe");
    }

    @Override
    public void onBlockBreak(Player player, BlockBreakEvent event, ItemStack tool) {
        Block startBlock = event.getBlock();

        if (this.isProtected(player, startBlock.getLocation()))
            return;

        if (!isLogBlock(startBlock)) {
            return;
        }

        Set<Block> treeBlocks = getTreeBlocks(startBlock);
        Location centerLoc = startBlock.getLocation();

        try {
            player.playSound(centerLoc, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
        } catch (Exception e) {
            try {
                player.playSound(centerLoc, Sound.valueOf("LEVEL_UP"), 0.5f, 2.0f);
            } catch (Exception ignored) {}
        }

        boolean instantBreak = ConfigSelector.TREECHOPPER.getConfig().getBoolean("instant-break", false);

        int delay = 0;
        for (Block block : treeBlocks) {
            if (this.isProtected(player, block.getLocation()))
                continue;

            if (BlacklistManager.getInstance().isBlacklistedBlock(block))
                continue;

            int currentDelay = instantBreak ? 0 : delay;
            Commons.getFoliaLib().getScheduler().runAtLocationLater(block.getLocation(), () -> {
                breakBlock(block, tool);
            }, currentDelay);

            if (!instantBreak)
                ++delay;
        }
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

    private Set<Block> getTreeBlocks(Block startBlock) {
        Set<Block> treeBlocks = new HashSet<>();
        Queue<Block> toCheck = new LinkedList<>();
        toCheck.add(startBlock);
        treeBlocks.add(startBlock);

        while (!toCheck.isEmpty() && treeBlocks.size() < 500) {
            Block current = toCheck.poll();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        Block relative = current.getRelative(x, y, z);
                        if (isTreeBlock(relative) && !treeBlocks.contains(relative)) {
                            treeBlocks.add(relative);
                            toCheck.add(relative);
                        }
                    }
                }
            }
        }

        return treeBlocks;
    }
}
