package me.serbob.xaltools.commands.args;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface CommandArgs {
    boolean execute(CommandSender sender, String label, String[] args);
    List<String> tabComplete(CommandSender sender, String[] args);
}
