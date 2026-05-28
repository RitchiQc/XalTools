package me.serbob.xaltools.commands.args.impl;

import me.serbob.xaltools.commands.args.CommandArgs;
import me.serbob.xaltools.manager.SelfDestructManager;
import me.serbob.commons.utils.message.ChatUtil;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SelfDestruct implements CommandArgs {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtil.c("&cThis command can only be used by players!"));
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("xaltools.selfdestruct")) {
            player.sendMessage(ChatUtil.c("&cYou don't have permission to use this command!"));
            return false;
        }

        if (args.length < 2) {
            player.sendMessage(ChatUtil.c("&cUsage: /xaltools selfdestruct <time> or /xaltools selfdestruct -1 <time>"));
            return false;
        }

        ItemStack item = player.getInventory().getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatUtil.c("&cYou must be holding an item!"));
            return false;
        }

        boolean isDelayed = false;
        String timeStr;

        if (args[1].equals("-1")) {
            if (args.length < 3) {
                player.sendMessage(ChatUtil.c("&cUsage: /xaltools selfdestruct -1 <time>"));
                return false;
            }
            isDelayed = true;
            timeStr = args[2];
        } else {
            timeStr = args[1];
        }

        long destructTime = SelfDestructManager.getInstance().parseTime(timeStr);
        if (destructTime == -1) {
            player.sendMessage(ChatUtil.c("&cInvalid time format! Use: 1d, 5h, 30m"));
            return false;
        }

        SelfDestructManager.getInstance().addTimedItem(player, item, destructTime, isDelayed);

        String message = isDelayed
                ? "&aTimer added! Will start counting down after 1 minute."
                : "&aTimer added to item!";
        player.sendMessage(ChatUtil.c(message));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        switch (args.length - 1) {
            case 1:
                return Arrays.asList("-1", "1d", "5h", "30m", "10m");

            case 2:
                if (args[1].equals("-1")) {
                    return Arrays.asList("1d", "5h", "30m", "10m");
                }
                break;
        }

        return Collections.emptyList();
    }
}
