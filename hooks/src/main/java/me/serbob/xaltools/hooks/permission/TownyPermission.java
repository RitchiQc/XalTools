package me.serbob.xaltools.hooks.permission;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.serbob.xaltools.api.permission.PermissionHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TownyPermission extends PermissionHook {

    @Override
    public String pluginName() {
        return "Towny";
    }

    @Override
    public boolean isBlocked(
            Player player,
            Location location
    ) {
        Town town = TownyAPI.getInstance().getTown(location);

        return town == null || !town.hasResident(player.getName());
    }
}
