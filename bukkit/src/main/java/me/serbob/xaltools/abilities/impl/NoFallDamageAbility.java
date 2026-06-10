package me.serbob.xaltools.abilities.impl;

import me.serbob.xaltools.abilities.AbstractAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class NoFallDamageAbility extends AbstractAbility {
    private final EquipmentSlot slot;

    public enum EquipmentSlot {
        HAND, FEET, LEGS, CHEST, HEAD, ANY_ARMOR
    }

    public NoFallDamageAbility(String nbt, EquipmentSlot slot) {
        super(nbt);
        this.slot = slot;
    }

    public boolean hasAbilityEquipped(Player player) {
        switch (slot) {
            case HAND:
                return hasAbility(player.getInventory().getItemInMainHand());
            case FEET:
                return hasAbility(player.getInventory().getBoots());
            case LEGS:
                return hasAbility(player.getInventory().getLeggings());
            case CHEST:
                return hasAbility(player.getInventory().getChestplate());
            case HEAD:
                return hasAbility(player.getInventory().getHelmet());
            case ANY_ARMOR:
                return hasAbility(player.getInventory().getBoots())
                        || hasAbility(player.getInventory().getLeggings())
                        || hasAbility(player.getInventory().getChestplate())
                        || hasAbility(player.getInventory().getHelmet());
            default:
                return false;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled())
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;

        Player player = (Player) event.getEntity();

        if (!hasAbilityEquipped(player))
            return;

        event.setCancelled(true);
    }

    @Override
    public void onBlockBreak(Player player, BlockBreakEvent event, ItemStack tool) {
        // No fall damage ability doesn't handle block break
    }

    @Override
    protected boolean shouldSkipDefaultDurabilityReduction(Player player, BlockBreakEvent event, ItemStack tool) {
        return true;
    }

    @Override
    public void onInteract(Player player, PlayerInteractEvent event, ItemStack tool) {
        // No fall damage ability doesn't handle interact
    }

    @Override
    public void onBucketFill(Player player, PlayerBucketFillEvent event, ItemStack tool) {
        // No fall damage ability doesn't handle bucket fill
    }
}
