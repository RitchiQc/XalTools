package me.serbob.mythictools.hooks.shop;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.objects.ShopItem;
import me.serbob.mythictools.api.shop.ShopHook;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EconomyShopGuiBridge implements ShopHook {
    @Override
    public String getName() {
        return "EconomyShopGUI";
    }

    @Override
    public void init() {

    }

    @Override
    public double getSellPrice(Material material) {
        ShopItem shopItem = EconomyShopGUIHook.getShopItem(new ItemStack(material));
        if (shopItem == null) return -1;

        double sellPrice = shopItem.getSellPrice();

        return sellPrice > 0 ? sellPrice : -1;
    }

    @Override
    public double getSellPrice(Player player, Material material) {
        return getSellPrice(material);
    }
}
