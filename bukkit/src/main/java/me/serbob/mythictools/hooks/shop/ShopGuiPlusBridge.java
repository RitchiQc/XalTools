package me.serbob.mythictools.hooks.shop;

import me.serbob.mythictools.api.shop.ShopHook;
import me.serbob.mythictools.api.shop.ShopManager;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopGuiPlusBridge implements ShopHook {
    @Override
    public String getName() {
        return "ShopGUI+";
    }

    @Override
    public void init() {
    }

    @Override
    public double getSellPrice(Material material) {
        double sellPrice = ShopGuiPlusApi.getItemStackPriceSell(new ItemStack(material));

        return sellPrice;
    }

    @Override
    public double getSellPrice(Player player, Material material) {
        double sellPrice = Math.max(0, ShopGuiPlusApi.getItemStackPriceSell(player, new ItemStack(material)));

        return sellPrice;
    }
}
