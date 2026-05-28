package me.serbob.xaltools;

import me.serbob.xaltools.api.interfaces.IHooksJava;
import me.serbob.xaltools.api.permission.PermissionHook;
import me.serbob.xaltools.hooks.permission.GriefPreventionPermission;
import me.serbob.xaltools.hooks.permission.LandsPermission;
import me.serbob.xaltools.hooks.permission.TownyPermission;
import me.serbob.xaltools.hooks.permission.WorldGuardPermission;

import java.util.HashMap;
import java.util.Map;

public class Hooks implements IHooksJava {

    @Override
    public Map<String, Class<? extends PermissionHook>> getPermissionHooks() {
        Map<String, Class<? extends PermissionHook>> hooks = new HashMap<>() {{
            put("WorldGuard", WorldGuardPermission.class);
            put("GriefPrevention", GriefPreventionPermission.class);
            put("Lands", LandsPermission.class);
            put("Towny", TownyPermission.class);
        }};

        return hooks;
    }
}
