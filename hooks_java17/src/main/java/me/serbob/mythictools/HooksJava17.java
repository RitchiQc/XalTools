package me.serbob.mythictools;

import me.serbob.mythictools.api.interfaces.IHooksJava;
import me.serbob.mythictools.api.permission.PermissionHook;
import me.serbob.mythictools.hooks.permission.GriefPreventionPermission;
import me.serbob.mythictools.hooks.permission.LandsPermission;
import me.serbob.mythictools.hooks.permission.TownyPermission;
import me.serbob.mythictools.hooks.permission.WorldGuardPermission;

import java.util.HashMap;
import java.util.Map;

public class HooksJava17 implements IHooksJava {

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
