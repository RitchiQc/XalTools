package me.serbob.xaltools.hooks;

import me.serbob.xaltools.api.permission.PermissionHook;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HooksLoader {

    private static final String HOOKS_CLASS = "me.serbob.xaltools.Hooks";

    public static List<Map.Entry<String, Class<? extends PermissionHook>>> loadHooks() {
        List<Map.Entry<String, Class<? extends PermissionHook>>> allHooks = new ArrayList<>();

        try {
            Class<?> hooksClass = Class.forName(HOOKS_CLASS);
            Object hooksInstance = hooksClass.getDeclaredConstructor().newInstance();

            Method getPermissionHooksMethod = hooksClass.getMethod("getPermissionHooks");
            Map<String, Class<? extends PermissionHook>> permissionHooks =
                    (Map<String, Class<? extends PermissionHook>>) getPermissionHooksMethod.invoke(hooksInstance);

            if (permissionHooks != null && !permissionHooks.isEmpty()) {
                allHooks.addAll(permissionHooks.entrySet());
                Bukkit.getLogger().info("[XalTools] Loaded " + permissionHooks.size() + " permission hooks");
            } else {
                Bukkit.getLogger().warning("[XalTools] No permission hooks found in Hooks class");
            }
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().warning("[XalTools] Hooks class not found: " + HOOKS_CLASS);
        } catch (Exception e) {
            Bukkit.getLogger().severe("[XalTools] Error loading permission hooks: " + e.getMessage());
            e.printStackTrace();
        }

        return allHooks;
    }
}
