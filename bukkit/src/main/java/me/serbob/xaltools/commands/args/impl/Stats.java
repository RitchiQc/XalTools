package me.serbob.xaltools.commands.args.impl;

import me.serbob.commons.utils.message.ChatUtil;
import me.serbob.xaltools.commands.args.CommandArgs;
import me.serbob.xaltools.manager.ItemTrackerManager;
import me.serbob.xaltools.tools.Tools;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Stats implements CommandArgs {
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Map<String, Integer> stats = ItemTrackerManager.getInstance().getStats();
        int total = ItemTrackerManager.getInstance().getTotalCount();

        sender.sendMessage(ChatUtil.c("&7=== XalTools Stats (Estimated) ==="));

        for (Tools tool : Tools.values()) {
            String name = tool.name().toLowerCase();
            int count = stats.getOrDefault(name, 0);
            sender.sendMessage(ChatUtil.c("&e" + capitalize(name) + " estimated: &f" + count));
        }

        sender.sendMessage(ChatUtil.c("&7Total estimated: &f" + total + " items"));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
