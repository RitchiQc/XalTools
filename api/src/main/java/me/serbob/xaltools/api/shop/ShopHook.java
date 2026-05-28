package me.serbob.xaltools.api.shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface ShopHook {
    /**
     * Get the name of the shop plugin
     * @return Shop plugin name
     */
    String getName();

    /**
     * Initialize the shop hook
     */
    void init();

    /**
     * Get sell price for a specific material
     * @param material The material to get price for
     * @return The sell price, or -1 if not found
     */
    double getSellPrice(Material material);

    /**
     * Get sell price for a specific material and player (for player-specific prices)
     * @param player The player
     * @param material The material
     * @return The sell price, or -1 if not found
     */
    double getSellPrice(Player player, Material material);
}
