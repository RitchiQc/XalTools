package me.serbob.mythictools.abilities.impl;

import me.serbob.mythictools.abilities.AbstractAbility;
import me.serbob.mythictools.manager.BlacklistManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Shovel3x3Ability extends AbstractAbility {
    public Shovel3x3Ability() {
        super("amethyst_shovel");
    }

    @Override
    public void onBlockBreak(Player player, BlockBreakEvent event, ItemStack tool) {
        Block centerBlock = event.getBlock();
        BlockFace face = getTargetBlockFace(player, centerBlock);
        List<Block> blocksToBreak = get3x3Blocks(centerBlock, face);

        for (Block block : blocksToBreak) {
            if (this.isProtected(player, block.getLocation()))
                continue;

            if (BlacklistManager.getInstance().isBlacklistedBlock(block))
                continue;

            if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                Location particleLoc = block.getLocation().add(0.5, 0.5, 0.5);

                block.getWorld().spawnParticle(Particle.PORTAL,
                        particleLoc, 8, 0.1, 0.1, 0.1, 0.01);
                block.getWorld().spawnParticle(Particle.REVERSE_PORTAL,
                        particleLoc, 5, 0.15, 0.15, 0.15, 0.005);
                block.getWorld().spawnParticle(Particle.WITCH,
                        particleLoc, 3, 0.1, 0.1, 0.1, 0);

                block.getWorld().playSound(particleLoc, Sound.BLOCK_SAND_BREAK, 0.5f, 1.2f);
                block.breakNaturally(tool);
            }
        }
    }

    @Override
    public void onInteract(Player player, PlayerInteractEvent event, ItemStack tool) {}

    @Override
    public void onBucketFill(Player player, PlayerBucketFillEvent event, ItemStack tool) {}

    private BlockFace getTargetBlockFace(Player player, Block block) {
        RayTraceResult result = player.rayTraceBlocks(6);
        if (result != null && result.getHitBlockFace() != null) {
            return result.getHitBlockFace();
        }

        Vector direction = player.getLocation().getDirection();
        double absX = Math.abs(direction.getX());
        double absY = Math.abs(direction.getY());
        double absZ = Math.abs(direction.getZ());

        if (absY > absX && absY > absZ) {
            return direction.getY() > 0 ? BlockFace.UP : BlockFace.DOWN;
        } else if (absX > absZ) {
            return direction.getX() > 0 ? BlockFace.EAST : BlockFace.WEST;
        } else {
            return direction.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        }
    }

    private List<Block> get3x3Blocks(Block center, BlockFace face) {
        List<Block> blocks = new ArrayList<>();

        switch (face) {
            case UP:
            case DOWN:
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        blocks.add(center.getRelative(x, 0, z));
                    }
                }
                break;
            case NORTH:
            case SOUTH:
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        blocks.add(center.getRelative(x, y, 0));
                    }
                }
                break;
            case EAST:
            case WEST:
                for (int z = -1; z <= 1; z++) {
                    for (int y = -1; y <= 1; y++) {
                        blocks.add(center.getRelative(0, y, z));
                    }
                }
                break;
        }

        return blocks;
    }
}
