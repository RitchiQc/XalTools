package me.serbob.mythictools.abilities.custom;

import lombok.Getter;
import me.serbob.mythictools.abilities.AbstractAbility;
import me.serbob.commons.utils.nbt.NBTUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomAbility extends AbstractAbility {
    @Getter private final CustomAbilityType type;
    @Getter private final String displayName;
    @Getter private final int potionDuration;
    @Getter private final int potionAmplifier;
    @Getter private final EquipmentSlot slot;

    private final Map<UUID, Boolean> activeEffects = new HashMap<>();

    public enum EquipmentSlot {
        HAND, FEET, LEGS, CHEST, HEAD, ANY_ARMOR
    }

    public CustomAbility(String nbt, CustomAbilityType type, String displayName,
                         int potionDuration, int potionAmplifier, EquipmentSlot slot) {
        super(nbt);
        this.type = type;
        this.displayName = displayName;
        this.potionDuration = potionDuration;
        this.potionAmplifier = potionAmplifier;
        this.slot = slot;
    }

    @Override
    public boolean hasAbility(ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return false;

        return NBTUtils.getInstance().hasNbt(item, getNbt());
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

        if (!type.handlesFallDamage())
            return;

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;

        Player player = (Player) event.getEntity();

        if (!hasAbilityEquipped(player))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!type.isPotionEffect())
            return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        boolean hadEffect = activeEffects.getOrDefault(playerId, false);
        boolean hasEffectNow = hasAbilityEquipped(player);

        if (hasEffectNow && !hadEffect) {
            applyPotionEffect(player);
            activeEffects.put(playerId, true);
        } else if (!hasEffectNow && hadEffect) {
            removePotionEffect(player);
            activeEffects.put(playerId, false);
        }
    }

    public void checkAndApplyEffect(Player player) {
        if (!type.isPotionEffect())
            return;

        UUID playerId = player.getUniqueId();
        boolean hasEffectNow = hasAbilityEquipped(player);
        boolean hadEffect = activeEffects.getOrDefault(playerId, false);

        if (hasEffectNow && !hadEffect) {
            applyPotionEffect(player);
            activeEffects.put(playerId, true);
        } else if (!hasEffectNow && hadEffect) {
            removePotionEffect(player);
            activeEffects.put(playerId, false);
        }
    }

    private void applyPotionEffect(Player player) {
        PotionEffectType effectType = type.getPotionEffectType();
        if (effectType == null) return;

        PotionEffect effect = new PotionEffect(
                effectType,
                potionDuration,
                potionAmplifier,
                false,
                false,
                true
        );
        player.addPotionEffect(effect);
    }

    private void removePotionEffect(Player player) {
        PotionEffectType effectType = type.getPotionEffectType();
        if (effectType == null) return;

        player.removePotionEffect(effectType);
    }

    @Override
    public void onBlockBreak(Player player, BlockBreakEvent event, ItemStack tool) {
        // Custom abilities don't handle block break by default
    }

    @Override
    public void onInteract(Player player, PlayerInteractEvent event, ItemStack tool) {
        // Custom abilities don't handle interact by default
    }

    @Override
    public void onBucketFill(Player player, PlayerBucketFillEvent event, ItemStack tool) {
        // Custom abilities don't handle bucket fill by default
    }
}
