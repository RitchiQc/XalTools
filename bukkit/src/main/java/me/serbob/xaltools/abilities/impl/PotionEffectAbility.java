package me.serbob.xaltools.abilities.impl;

import me.serbob.commons.Commons;
import me.serbob.commons.utils.nbt.NBTUtils;
import me.serbob.xaltools.abilities.AbstractAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PotionEffectAbility extends AbstractAbility {
    private static final String AMPLIFIER_KEY = "potion_amplifier";
    private static final int DEFAULT_DURATION = 100; // 5 seconds, refreshed every 0.5 second

    private final PotionEffectType effectType;
    private final EquipmentSlot slot;
    private final Map<UUID, Boolean> activeEffects = new HashMap<>();

    public enum EquipmentSlot {
        HAND, FEET, LEGS, CHEST, HEAD, ANY_ARMOR
    }

    public PotionEffectAbility(String nbt, PotionEffectType effectType, EquipmentSlot slot) {
        super(nbt);
        this.effectType = effectType;
        this.slot = slot;
        startEffectChecker();
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
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (slot != EquipmentSlot.HAND)
            return;

        Player player = event.getPlayer();

        // Always check the current state after the slot change
        Commons.getFoliaLib().getScheduler().runAtEntityLater(player, () -> {
            checkAndApplyEffect(player);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (slot != EquipmentSlot.HAND)
            return;

        Player player = event.getPlayer();
        
        // Check after the drop is processed
        Commons.getFoliaLib().getScheduler().runAtEntityLater(player, () -> {
            checkAndApplyEffect(player);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        boolean couldAffectSlot = false;
        
        if (slot == EquipmentSlot.HAND) {
            // Check if click affects hotbar (slots 36-44 in player inventory)
            // or if player clicked in their own inventory to move item to hotbar
            if (event.getSlotType() == InventoryType.SlotType.QUICKBAR) {
                couldAffectSlot = true;
            }
            // Shift-click that might move item to hotbar
            if (event.isShiftClick() && event.getCurrentItem() != null && !event.getCurrentItem().getType().isAir()) {
                couldAffectSlot = true;
            }
            // Hotbar swap (number keys)
            if (event.getHotbarButton() >= 0) {
                couldAffectSlot = true;
            }
            // Click in hotbar range (raw slots 36-44 in player inventory)
            if (event.getRawSlot() >= 36 && event.getRawSlot() <= 44) {
                couldAffectSlot = true;
            }
        } else {
            // Armor slot logic
            if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                couldAffectSlot = true;
            }
            if (event.isShiftClick() && event.getCurrentItem() != null && !event.getCurrentItem().getType().isAir()) {
                couldAffectSlot = true;
            }
            if (event.getHotbarButton() >= 0 && event.getSlotType() == InventoryType.SlotType.ARMOR) {
                couldAffectSlot = true;
            }
            if (event.getRawSlot() >= 5 && event.getRawSlot() <= 8) {
                couldAffectSlot = true;
            }
        }

        if (!couldAffectSlot)
            return;

        Player player = (Player) event.getWhoClicked();

        // Run check next tick to let the inventory update
        Commons.getFoliaLib().getScheduler().runAtEntityLater(player, () -> {
            checkAndApplyEffect(player);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (slot == EquipmentSlot.HAND)
            return;

        // Check if player right-clicked to equip armor
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType().isAir())
            return;

        // Only check if the item could be armor
        String typeName = item.getType().name();
        boolean isArmor = typeName.endsWith("_HELMET") || typeName.endsWith("_CHESTPLATE") 
                || typeName.endsWith("_LEGGINGS") || typeName.endsWith("_BOOTS");
        
        if (!isArmor)
            return;

        // Run check next tick to let the armor equip
        Commons.getFoliaLib().getScheduler().runAtEntityLater(player, () -> {
            checkAndApplyEffect(player);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Reset the state so the effect gets applied on join
        activeEffects.put(player.getUniqueId(), false);
        
        // Check and apply effect
        Commons.getFoliaLib().getScheduler().runAtEntityLater(player, () -> {
            checkAndApplyEffect(player);
        }, 5L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Remove the effect when player quits
        if (activeEffects.getOrDefault(playerId, false)) {
            removePotionEffect(player);
        }
        
        // Clean up the map
        activeEffects.remove(playerId);
    }

    public void checkAndApplyEffect(Player player) {
        UUID playerId = player.getUniqueId();
        boolean hasEffectNow = hasAbilityEquipped(player);
        boolean hadEffect = activeEffects.getOrDefault(playerId, false);

        if (hasEffectNow) {
            // Always reapply to refresh duration, even if already had effect
            applyPotionEffect(player);
            activeEffects.put(playerId, true);
        } else if (hadEffect) {
            removePotionEffect(player);
            activeEffects.put(playerId, false);
        }
    }

    private void applyPotionEffect(Player player) {
        if (effectType == null) return;

        int amplifier = getAmplifier(player);

        PotionEffect effect = new PotionEffect(
                effectType,
                DEFAULT_DURATION,
                amplifier,
                false,
                true,
                true
        );
        player.addPotionEffect(effect, true); // true = force overwrite existing effect
    }

    private void removePotionEffect(Player player) {
        if (effectType == null) return;

        player.removePotionEffect(effectType);
    }

    private int getAmplifier(Player player) {
        ItemStack equippedItem = getEquippedItem(player);
        if (equippedItem == null) return 0;

        if (NBTUtils.getInstance().hasTag(equippedItem, AMPLIFIER_KEY)) {
            return NBTUtils.getInstance().getInt(equippedItem, AMPLIFIER_KEY);
        }

        return 0;
    }

    private ItemStack getEquippedItem(Player player) {
        switch (slot) {
            case HAND:
                return player.getInventory().getItemInMainHand();
            case FEET:
                return player.getInventory().getBoots();
            case LEGS:
                return player.getInventory().getLeggings();
            case CHEST:
                return player.getInventory().getChestplate();
            case HEAD:
                return player.getInventory().getHelmet();
            case ANY_ARMOR:
                ItemStack boots = player.getInventory().getBoots();
                if (hasAbility(boots)) return boots;
                ItemStack leggings = player.getInventory().getLeggings();
                if (hasAbility(leggings)) return leggings;
                ItemStack chestplate = player.getInventory().getChestplate();
                if (hasAbility(chestplate)) return chestplate;
                ItemStack helmet = player.getInventory().getHelmet();
                if (hasAbility(helmet)) return helmet;
                return null;
            default:
                return null;
        }
    }

    private void startEffectChecker() {
        // Use global timer but delegate to each player's regional thread for Folia compatibility
        Commons.getFoliaLib().getScheduler().runTimer(task -> {
            for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                Commons.getFoliaLib().getScheduler().runAtEntity(player, entityTask -> {
                    checkAndApplyEffect(player);
                });
            }
        }, 10L, 10L); // Check every 0.5 seconds
    }

    @Override
    public void onBlockBreak(Player player, BlockBreakEvent event, ItemStack tool) {
        // Potion effect abilities don't handle block break
    }

    @Override
    public void onInteract(Player player, PlayerInteractEvent event, ItemStack tool) {
        // Potion effect abilities don't handle interact
    }

    @Override
    public void onBucketFill(Player player, PlayerBucketFillEvent event, ItemStack tool) {
        // Potion effect abilities don't handle bucket fill
    }
}
