package me.serbob.xaltools.hooks;

import me.serbob.xaltools.Hooks;
import me.serbob.xaltools.api.permission.PermissionHook;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HooksLoader {

    public static List<Map.Entry<String, Class<? extends PermissionHook>>> loadHooks() {
        List<Map.Entry<String, Class<? extends PermissionHook>>> allHooks = new ArrayList<>();

        Hooks hooksInstance = new Hooks();
        Map<String, Class<? extends PermissionHook>> permissionHooks = hooksInstance.getPermissionHooks();

        if (permissionHooks != null && !permissionHooks.isEmpty()) {
            allHooks.addAll(permissionHooks.entrySet());
            Bukkit.getLogger().info("[XalTools] Loaded " + permissionHooks.size() + " permission hooks");
        } else {
            Bukkit.getLogger().warning("[XalTools] No permission hooks found in Hooks class");
        }

        return allHooks;
    }
}
