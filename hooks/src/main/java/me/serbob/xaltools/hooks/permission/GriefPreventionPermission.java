package me.serbob.xaltools.hooks.permission;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.serbob.xaltools.api.permission.PermissionHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GriefPreventionPermission extends PermissionHook {

    @Override
    public String pluginName() {
        return "GriefPrevention";
    }

    @Override
    public boolean isBlocked(
            Player player,
            Location location
    ) {
        return GriefPrevention.instance.allowBreak(player, location.getBlock(), location) != null;
    }
}
