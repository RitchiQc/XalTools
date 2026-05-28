package me.serbob.xaltools.commands.args.impl;

import me.serbob.commons.utils.debug.Timer;
import me.serbob.commons.utils.message.ChatUtil;
import me.serbob.xaltools.XalTools;
import me.serbob.xaltools.commands.args.CommandArgs;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class Reload implements CommandArgs {
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Timer timer = new Timer().start();

        XalTools.getInstance().load();

        sender.sendMessage(ChatUtil.c("&aReloaded successfully in " + timer.get() + "ms!"));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
