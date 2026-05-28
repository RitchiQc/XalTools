package me.serbob.xaltools.hooks.shop;

import me.serbob.commons.Commons;
import me.serbob.xaltools.api.shop.ShopHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DonutWorthBridge implements ShopHook {
    private Class<?> pricesClass;
    private Method getPriceMethod;
    private Method getPriceWithMultiplierMethod;
    private boolean hooked = false;

    private final Map<Material, Double> priceCache = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "DonutWorth";
    }

    @Override
    public void init() {
        try {
            pricesClass = Class.forName("me.serbob.donutworth.api.util.Prices");
            getPriceMethod = pricesClass.getDeclaredMethod("getPrice", ItemStack.class);
            getPriceMethod.setAccessible(true);
            getPriceWithMultiplierMethod = pricesClass.getDeclaredMethod("getPriceWithMultiplier", Player.class, ItemStack.class);
            getPriceWithMultiplierMethod.setAccessible(true);

            hooked = true;

            cacheAllPrices();

            Commons.getFoliaLib().getScheduler().runTimerAsync(
                    this::cacheAllPrices,
                    2400L,
                    2400L
            );

            Bukkit.getLogger().info("[XalTools] Successfully hooked into DonutWorth with " + priceCache.size() + " items cached!");
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().warning("[XalTools] DonutWorth not found, hook disabled.");
        } catch (NoSuchMethodException e) {
            Bukkit.getLogger().warning("[XalTools] DonutWorth API has changed, hook disabled.");
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[XalTools] Failed to hook into DonutWorth", e);
        }
    }

    private void cacheAllPrices() {
        if (!hooked) return;

        Map<Material, Double> newCache = new HashMap<>();

        for (Material material : Material.values()) {
            if (material.isItem() && !material.isAir()) {
                try {
                    ItemStack testItem = new ItemStack(material);
                    Object priceObj = getPriceMethod.invoke(null, testItem);

                    if (priceObj instanceof Double) {
                        double price = (Double) priceObj;
                        if (price > 0) {
                            newCache.put(material, price);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        priceCache.clear();
        priceCache.putAll(newCache);
    }

    @Override
    public double getSellPrice(Material material) {
        if (!hooked) return 0.0;

        Double cachedPrice = priceCache.get(material);
        if (cachedPrice != null) return cachedPrice;

        try {
            ItemStack item = new ItemStack(material);
            Object priceObj = getPriceMethod.invoke(null, item);

            if (priceObj instanceof Double) {
                double price = (Double) priceObj;
                if (price > 0) {
                    priceCache.put(material, price);
                }
                return price;
            }
        } catch (Exception e) {
        }

        return 0.0;
    }

    @Override
    public double getSellPrice(Player player, Material material) {
        if (!hooked) return 0.0;

        try {
            ItemStack item = new ItemStack(material);
            Object priceObj = getPriceWithMultiplierMethod.invoke(null, player, item);

            if (priceObj instanceof Double) {
                return (Double) priceObj;
            }
        } catch (Exception e) {
        }

        return getSellPrice(material);
    }

    public double getSellPrice(ItemStack itemStack) {
        if (!hooked || itemStack == null) return 0.0;

        try {
            Object priceObj = getPriceMethod.invoke(null, itemStack);
            if (priceObj instanceof Double) {
                return (Double) priceObj;
            }
        } catch (Exception e) {
        }

        return getSellPrice(itemStack.getType());
    }

    public boolean isHooked() {
        return hooked;
    }

    public int getCachedItemCount() {
        return priceCache.size();
    }
}
