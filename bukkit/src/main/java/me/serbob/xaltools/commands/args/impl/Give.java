package me.serbob.xaltools.commands.args.impl;

import me.serbob.xaltools.commands.args.CommandArgs;
import me.serbob.xaltools.manager.ItemTrackerManager;
import me.serbob.xaltools.manager.SelfDestructManager;
import me.serbob.xaltools.manager.ToolManager;
import me.serbob.xaltools.tools.AbstractTool;
import me.serbob.commons.utils.message.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Give implements CommandArgs {
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatUtil.c("&cUsage: /xaltools give <tool> <player>"));
            return false;
        }

        String toolStr = args[1].toLowerCase();
        String playerName = args[2];
        long destructTime = -1;

        if (args.length > 3) {
            destructTime = SelfDestructManager.getInstance().parseTime(args[3]);
            if (destructTime == -1) {
                sender.sendMessage(ChatUtil.c("&cInvalid time format! Use: 1d, 5h, 30m"));
                return false;
            }
        }

        ToolManager toolManager = ToolManager.getInstance();
        AbstractTool tool = toolManager.getTools().get(toolStr);

        if (tool == null) {
            sender.sendMessage(ChatUtil.c("&cInvalid tool! Available tools: " +
                    String.join(", ", toolManager.getTools().keySet())));
            return false;
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatUtil.c("&cPlayer not found!"));
            return false;
        }

        ItemStack item = tool.parseItem();

        if (destructTime != -1) {
            SelfDestructManager.getInstance().addTimedItem(player, item, destructTime, false);
        }

        player.getInventory().addItem(item);

        long expiresAt = -1;
        if (destructTime != -1) {
            expiresAt = System.currentTimeMillis() + destructTime;
        }
        ItemTrackerManager.getInstance().addItem(toolStr, player.getUniqueId(), expiresAt > 0 ? expiresAt : null);

        sender.sendMessage(ChatUtil.c("&aTool given successfully to " + player.getName()));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        switch (args.length - 1) {
            case 1:
                return new ArrayList<>(ToolManager.getInstance().getTools().keySet());

            case 2:
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());

            case 3:
                return Arrays.asList("1d", "5h", "30m", "10m");
        }

        return Collections.emptyList();
    }
}
