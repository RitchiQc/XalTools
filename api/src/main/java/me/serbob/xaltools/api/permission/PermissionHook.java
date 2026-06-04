package me.serbob.xaltools.api.permission;

import me.serbob.commons.enums.ConfigSelector;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class PermissionHook {
    public abstract String pluginName();

    public boolean shouldLoad() {
        if (pluginName().isEmpty())
            return true;

        String hookPath = "hooks." + pluginName().toLowerCase();

        if (!ConfigSelector.HOOKS.getConfig().contains(hookPath)) {
            ConfigSelector.HOOKS.getConfig().set(hookPath, true);
        }

        return ConfigSelector.HOOKS.getConfig().getBoolean(hookPath, true);
    }

    public abstract boolean isBlocked(Player player, Location location);
}
