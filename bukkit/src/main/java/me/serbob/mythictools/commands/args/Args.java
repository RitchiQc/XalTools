package me.serbob.mythictools.commands.args;

import lombok.Getter;
import me.serbob.mythictools.commands.args.impl.Give;
import me.serbob.mythictools.commands.args.impl.Hooks;
import me.serbob.mythictools.commands.args.impl.Reload;
import me.serbob.mythictools.commands.args.impl.SelfDestruct;

import java.util.Arrays;

public enum Args {
    GIVE("give", new Give()),
    SELFDESTRUCT("selfdestruct", new SelfDestruct()),
    HOOKS("hooks", new Hooks()),
    RELOAD("reload", new Reload())
    ;

    @Getter
    private final String commandName;
    private final CommandArgs instance;

    Args(String commandName, CommandArgs instance) {
        this.commandName = commandName;
        this.instance = instance;
    }

    public CommandArgs get() {
        return instance;
    }

    public static Args fromString(String cmd) {
        return Arrays.stream(values())
                .filter(arg -> arg.getCommandName().equals(cmd.toLowerCase()))
                .findFirst()
                .orElse(null);
    }
}
