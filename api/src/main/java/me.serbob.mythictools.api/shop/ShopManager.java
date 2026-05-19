package me.serbob.mythictools.api.shop;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShopManager {
    @Getter(lazy = true)
    private static final ShopManager instance = new ShopManager();

    @Getter
    private final Map<String, ShopHook> registeredShops = new HashMap<>();
    private ShopHook activeShop = null;

    /**
     * Register a new shop integration
     * @param shopHook The shop hook to register
     */
    public void registerShop(ShopHook shopHook) {
        registeredShops.put(shopHook.getName().toLowerCase(), shopHook);
    }

    /**
     * Set active shop from config
     * @param shopName Shop name from config
     * @return true if shop was set successfully
     */
    public boolean setActiveShop(String shopName) {
        if (shopName.equalsIgnoreCase("none")) {
            activeShop = null;
            return true;
        }

        ShopHook shop = registeredShops.get(shopName.toLowerCase());
        if (shop != null) {
            activeShop = shop;
            shop.init();
            return true;
        }
        return false;
    }

    public void init() {
        if (activeShop == null)
            return;

        activeShop.init();
    }

    /**
     * Get price for a material
     * @param material Material to check
     * @return Price or -1 if no shop active
     */
    public double getPrice(Material material) {
        if (activeShop == null) return -1;
        return activeShop.getSellPrice(material);
    }

    /**
     * Get price for a player and material
     * @param player Player to check
     * @param material Material to check
     * @return Price or -1 if no shop active
     */
    public double getPrice(Player player, Material material) {
        if (activeShop == null) return -1;
        return activeShop.getSellPrice(player, material);
    }

    /**
     * Get currently active shop
     * @return Active shop or null if none
     */
    public ShopHook getActiveShop() {
        return activeShop;
    }

    public boolean hasAnActiveShop() {
        return activeShop != null;
    }
}
