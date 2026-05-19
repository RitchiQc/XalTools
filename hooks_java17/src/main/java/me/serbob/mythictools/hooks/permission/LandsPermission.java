package me.serbob.mythictools.hooks.permission;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.type.RoleFlag;
import me.angeschossen.lands.api.land.LandWorld;
import me.serbob.mythictools.MythicTools;
import me.serbob.mythictools.api.permission.PermissionHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LandsPermission extends PermissionHook {

    private LandsIntegration api;

    private RoleFlag breakBlock = null;

    public LandsPermission() {
        api = LandsIntegration.of(MythicTools.getInstance());
        breakBlock = api.getFlagRegistry().getRoleFlags().stream().filter(roleFlag
                -> roleFlag.getName().equals("block_break")).findFirst().orElse(null);
    }

    @Override
    public String pluginName() {
        return "Lands";
    }

    @Override
    public boolean isProtected(
            Player player,
            Location location
    ) {
        LandWorld world = api.getWorld(location.getWorld());

        if (world == null)
            return false;

        return !world.hasRoleFlag(player.getUniqueId(), location, breakBlock);
    }
}
