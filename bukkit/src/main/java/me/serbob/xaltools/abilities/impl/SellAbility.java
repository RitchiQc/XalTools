package me.serbob.xaltools.abilities.impl;

import me.serbob.xaltools.abilities.AbstractAbility;
import me.serbob.xaltools.api.currency.CurrencyManager;
import me.serbob.xaltools.api.shop.ShopManager;
import me.serbob.commons.enums.Messages;
import me.serbob.commons.utils.number.NumberUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SellAbility extends AbstractAbility {
    public SellAbility() {
        super("sell_axe");
    }

    @Override
    public void onBlockBreak(Player player, BlockBreakEvent event, ItemStack tool) {
        Block block = event.getBlock();

        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
            return;

        event.setCancelled(true);

        Chest chest = (Chest) block.getState();
        Inventory chestInventory = chest.getInventory();

        if (chestInventory instanceof DoubleChestInventory) {
            DoubleChestInventory doubleChest = (DoubleChestInventory) chestInventory;
            DoubleChest doubleChestHolder = doubleChest.getHolder();

            if (doubleChestHolder != null) {
                processSellAxeUse(player, doubleChest, doubleChest.getContents());
            }
        } else {
            processSellAxeUse(player, chestInventory, chestInventory.getContents());
        }
    }

    @Override
    public void onInteract(Player player, PlayerInteractEvent event, ItemStack tool) {}

    @Override
    public void onBucketFill(Player player, PlayerBucketFillEvent event, ItemStack tool) {}

    protected void processSellAxeUse(Player player, Inventory chestInventory, ItemStack[] items) {
        if (items.length == 0) {
            return;
        }

        sellChestContents(player, chestInventory, items);
    }

    private void sellChestContents(Player player, Inventory chestInventory, ItemStack[] items) {
        double totalPrice = 0;
        List<Integer> slotsToRemove = new ArrayList<>();

        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null || item.getType() == Material.AIR)
                continue;

            double itemPrice = ShopManager.getInstance().getPrice(player, item.getType());

            if (itemPrice > 0) {
                totalPrice += itemPrice * item.getAmount();
                slotsToRemove.add(i);
            }
        }

        if (totalPrice <= 0) {
            return;
        }

        for (Integer slot : slotsToRemove) {
            chestInventory.setItem(slot, null);
        }

        CurrencyManager.getInstance().deposit(player, totalPrice);
        Messages.SELLAXE_SOLD.sendBoth(player,
                "{money}", NumberUtil.formatPrice(totalPrice)
        );
    }
}
