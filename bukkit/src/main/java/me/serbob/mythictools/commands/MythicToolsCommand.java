package me.serbob.mythictools.commands;

import me.serbob.mythictools.commands.args.Args;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MythicToolsCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission("mythictools.admin"))
            return false;

        if (args.length == 0) {
            return false;
        }

        String subCommand = args[0].toLowerCase();

        Args argHandler = Args.fromString(subCommand);
        if (argHandler == null) {
            return false;
        }

        return argHandler.get().execute(sender, label, args);
    }


    @Override
    public @Nullable List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String s,
            String[] args
    ) {
        if (args.length == 1) {
            List<String> completions = Arrays.stream(Args.values())
                    .map(arg -> arg.name().toLowerCase()).collect(Collectors.toList());

            return filterCompletions(completions, args[0]);
        }

        try {
            Args argHandler = Args.valueOf(args[0].toUpperCase());

            return filterCompletions(
                    argHandler.get().tabComplete(sender, args),
                    args[args.length - 1]
            );
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private List<String> filterCompletions(List<String> completions, String prefix) {
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
