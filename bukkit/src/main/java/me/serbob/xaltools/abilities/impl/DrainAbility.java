package me.serbob.xaltools.abilities.impl;

import me.serbob.xaltools.abilities.AbstractAbility;
import me.serbob.xaltools.manager.BlacklistManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

public class DrainAbility extends AbstractAbility {
    public DrainAbility() {
        super("amethyst_bucket");
    }

    @Override
    public void onBlockBreak(Player player, BlockBreakEvent event, ItemStack tool) {
        event.setCancelled(true);
    }

    @Override
    public void onInteract(Player player, PlayerInteractEvent event, ItemStack tool) {
        Block targetBlock = null;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clicked = event.getClickedBlock();
            if (clicked == null) return;

            if (clicked.getType() == Material.WATER || clicked.getType() == Material.LAVA) {
                targetBlock = clicked;
            } else {
                targetBlock = clicked.getRelative(event.getBlockFace());
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            Block lookingAt = getTargetBlock(player, 5);
            if (lookingAt != null && (lookingAt.getType() == Material.WATER || lookingAt.getType() == Material.LAVA)) {
                targetBlock = lookingAt;
            }
        }

        if (targetBlock == null) return;

        if (hasWaterOrLavaNearby(targetBlock)) {
            event.setCancelled(true);

            if (BlacklistManager.getInstance().isBlacklistedBlock(targetBlock))
                return;

            handleWaterDrain(player, targetBlock);

            Location centerLoc = targetBlock.getLocation();
            player.playSound(centerLoc, Sound.ITEM_BUCKET_FILL, 1.0f, 0.8f);
            player.playSound(centerLoc, Sound.ENTITY_PLAYER_SPLASH, 0.5f, 1.2f);

            centerLoc.getWorld().spawnParticle(Particle.BUBBLE,
                    centerLoc.add(0.5, 0.5, 0.5), 50, 1.5, 1.5, 1.5, 0.05);
        }
    }

    @Override
    public void onBucketFill(Player player, PlayerBucketFillEvent event, ItemStack tool) {
        event.setCancelled(true);

        Block targetBlock = event.getBlockClicked().getRelative(event.getBlockFace());

        if (BlacklistManager.getInstance().isBlacklistedBlock(targetBlock))
            return;

        handleWaterDrain(player, targetBlock);

        Location centerLoc = targetBlock.getLocation();
        player.playSound(centerLoc, Sound.ITEM_BUCKET_FILL, 1.0f, 0.8f);
        player.playSound(centerLoc, Sound.ENTITY_PLAYER_SPLASH, 0.5f, 1.2f);

        centerLoc.getWorld().spawnParticle(Particle.BUBBLE,
                centerLoc.add(0.5, 0.5, 0.5), 50, 1.5, 1.5, 1.5, 0.05);
    }

    private Block getTargetBlock(Player player, int range) {
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if (lastBlock.getType() != Material.AIR) {
                return lastBlock;
            }
        }
        return lastBlock;
    }

    private void handleWaterDrain(Player player, Block targetBlock) {
        if (targetBlock == null) return;
        int radius = 1;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = targetBlock.getRelative(x, y, z);
                    if (block.getType() == Material.WATER || block.getType() == Material.LAVA) {
                        Location particleLoc = block.getLocation().add(0.5, 0.5, 0.5);

                        block.getWorld().spawnParticle(Particle.PORTAL,
                                particleLoc, 6, 0.2, 0.2, 0.2, 0.01);
                        block.getWorld().spawnParticle(Particle.SPLASH,
                                particleLoc, 15, 0.3, 0.3, 0.3, 0);
                        block.getWorld().spawnParticle(Particle.WITCH,
                                particleLoc, 3, 0.15, 0.15, 0.15, 0);

                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }

    private boolean hasWaterOrLavaNearby(Block center) {
        int radius = 1;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getRelative(x, y, z);
                    if (block.getType() == Material.WATER || block.getType() == Material.LAVA) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
