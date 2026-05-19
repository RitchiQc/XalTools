package me.serbob.mythictools.hooks.shop;

import me.serbob.commons.Commons;
import me.serbob.mythictools.api.shop.ShopHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PrimeSellerBridge implements ShopHook {
    private Class<?> mapBaseClass;
    private Class<?> sellItemClass;
    private Field databaseField;
    private Method getItemMethod;
    private Method getPriceMethod;
    private boolean hooked = false;

    private final Map<Material, Double> priceCache = new ConcurrentHashMap<>();
    private final Map<String, Double> itemStackCache = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "PrimeSeller";
    }

    @Override
    public void init() {
        try {
            mapBaseClass = Class.forName("ru.spigotmc.destroy.primeseller.configurations.database.MapBase");
            sellItemClass = Class.forName("ru.spigotmc.destroy.primeseller.configurations.database.SellItem");

            databaseField = mapBaseClass.getDeclaredField("database");
            databaseField.setAccessible(true);

            getItemMethod = sellItemClass.getDeclaredMethod("getItem");
            getItemMethod.setAccessible(true);

            getPriceMethod = sellItemClass.getDeclaredMethod("getPrice");
            getPriceMethod.setAccessible(true);

            hooked = true;

            refreshCache();

            Commons.getFoliaLib().getScheduler().runTimerAsync(
                    this::refreshCache,
                    6000L,
                    6000L
            );

            Bukkit.getLogger().info("[MythicTools] Successfully hooked into PrimeSeller with " + priceCache.size() + " items cached!");
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().warning("[MythicTools] PrimeSeller not found, hook disabled.");
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[MythicTools] Failed to hook into PrimeSeller", e);
        }
    }

    public void refreshCache() {
        if (!hooked) return;

        try {
            @SuppressWarnings("unchecked")
            LinkedHashMap<Integer, Object> database = (LinkedHashMap<Integer, Object>) databaseField.get(null);

            if (database == null || database.isEmpty()) return;

            Map<Material, Double> newPriceCache = new HashMap<>();
            Map<String, Double> newItemStackCache = new HashMap<>();

            for (Map.Entry<Integer, Object> entry : database.entrySet()) {
                Object sellItem = entry.getValue();
                if (sellItem == null) continue;

                ItemStack item = (ItemStack) getItemMethod.invoke(sellItem);
                if (item == null) continue;

                Double price = (Double) getPriceMethod.invoke(sellItem);
                if (price == null) continue;

                newPriceCache.put(item.getType(), price);

                String itemKey = item.toString();
                newItemStackCache.put(itemKey, price);
            }

            priceCache.clear();
            priceCache.putAll(newPriceCache);
            itemStackCache.clear();
            itemStackCache.putAll(newItemStackCache);

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[MythicTools] Error refreshing PrimeSeller cache", e);
        }
    }

    @Override
    public double getSellPrice(Material material) {
        if (!hooked) return 0.0;

        return priceCache.getOrDefault(material, 0.0);
    }

    @Override
    public double getSellPrice(Player player, Material material) {
        return getSellPrice(material);
    }

    public double getSellPrice(ItemStack itemStack) {
        if (!hooked || itemStack == null) return 0.0;

        String itemKey = itemStack.toString();
        Double exactPrice = itemStackCache.get(itemKey);
        if (exactPrice != null) return exactPrice;

        return getSellPrice(itemStack.getType());
    }

    public boolean isHooked() {
        return hooked;
    }

    public int getCachedItemCount() {
        return priceCache.size();
    }
}
