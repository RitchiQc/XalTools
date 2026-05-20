package me.serbob.mythictools.utils.item;

import me.serbob.commons.utils.message.ChatUtil;
import me.serbob.commons.utils.nbt.NBTUtils;
import me.serbob.mythictools.abilities.AbstractAbility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemUtil {
    public static ItemStack parseItem(FileConfiguration config, AbstractAbility[] ability) {
        Material material = Material.valueOf(config.getString("item.material"));
        String name = config.getString("item.name");
        List<String> lore = config.getStringList("item.lore");
        List<String> strEnchants = config.getStringList("item.enchants");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatUtil.c(name));
        meta.setLore(ChatUtil.c(lore));

        for (String strEnchant : strEnchants) {
            String[] parts = strEnchant.split(":");
            String enchantName = parts[0];
            int level = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

            try {
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
                if (enchantment == null) {
                    enchantment = Enchantment.getByName(enchantName.toUpperCase());
                }

                if (enchantment != null) {
                    meta.addEnchant(enchantment, level, true);
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("Error parsing enchantment '" + strEnchant + "': " + e.getMessage());
            }
        }

        item.setItemMeta(meta);

        for (AbstractAbility ability1 : ability) {
            NBTUtils.getInstance().modifyNbt(item, ability1.getNbt());
        }

        // Write potion amplifier if specified
        int potionAmplifier = config.getInt("potion_amplifier", -1);
        if (potionAmplifier >= 0) {
            NBTUtils.getInstance().setInt(item, "potion_amplifier", potionAmplifier);
        }

        return item;
    }

    public static boolean reduceDurability(Player player, ItemStack tool, int amount) {
        ItemStack currentTool = player.getInventory().getItemInHand();
        if (!currentTool.isSimilar(tool)) {
            return true;
        }

        ItemMeta meta = currentTool.getItemMeta();
        if (!(meta instanceof Damageable)) {
            return true;
        }

        Damageable damageable = (Damageable) meta;
        int currentDamage = damageable.getDamage();
        int newDamage = currentDamage + amount;
        int maxDurability = currentTool.getType().getMaxDurability();

        if (newDamage >= maxDurability) {
            player.getInventory().setItemInMainHand(null);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            return false;
        }

        damageable.setDamage(newDamage);
        currentTool.setItemMeta(meta);
        return true;
    }

    public static int getRemainingDurability(ItemStack tool) {
        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable)) {
            return -1;
        }

        Damageable damageable = (Damageable) meta;
        return tool.getType().getMaxDurability() - damageable.getDamage();
    }
}
