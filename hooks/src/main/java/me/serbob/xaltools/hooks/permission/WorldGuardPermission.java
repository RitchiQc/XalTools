package me.serbob.xaltools.hooks.permission;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.serbob.xaltools.api.permission.PermissionHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardPermission extends PermissionHook {

    @Override
    public String pluginName() {
        return "WorldGuard";
    }

    @Override
    public boolean isBlocked(
            Player player,
            Location location
    ) {
        com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        return query.queryState(wgLocation, localPlayer, Flags.BLOCK_BREAK) == StateFlag.State.DENY;
    }
}
