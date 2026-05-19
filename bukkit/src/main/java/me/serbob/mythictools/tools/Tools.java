package me.serbob.mythictools.tools;

import lombok.Getter;
import me.serbob.mythictools.manager.SelfDestructManager;
import me.serbob.mythictools.tools.impl.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum Tools {
    DRILL(new DrillPickaxe()),
    BUCKET(new Bucket()),
    SHOVEL(new Shovel()),
    TREECHOPPER(new TreeChopper()),
    MULTITOOL(new MultiTool()),
    SELLAXE(new SellAxe()),
    INFINITEFIREWORK(new InfiniteFirework())
    ;

    private final @Getter AbstractTool tool;

    Tools(AbstractTool tool) {
        this.tool = tool;
    }

    public static void giveTool(Player player, AbstractTool tool) {
        player.getInventory().addItem(tool.parseItem());
    }

    public static void giveTimedTool(Player player, AbstractTool tool, long duration) {
        ItemStack item = tool.parseItem();

        SelfDestructManager.getInstance().addTimedItem(player, item, duration, false);

        player.getInventory().addItem(item);
    }

    public static void giveTool(Player player, Tools tool) {
        player.getInventory().addItem(tool.getTool().parseItem());
    }

    public static void giveTimedTool(Player player, Tools tool, long duration) {
        ItemStack item = tool.getTool().parseItem();

        SelfDestructManager.getInstance().addTimedItem(player, item, duration, false);

        player.getInventory().addItem(item);
    }

    public static Tools toolFromString(String toolString) {
        return Tools.valueOf(toolString.toUpperCase());
    }
}
