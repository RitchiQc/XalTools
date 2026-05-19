package me.serbob.mythictools.api.permission;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PermissionManager {
    @Getter(lazy = true)
    private static final PermissionManager instance = new PermissionManager();

    private final @Getter Set<PermissionHook> activePermissionHooks = new HashSet<>();

    public void addPermissionHook(PermissionHook permissionHook) {
        if (!permissionHook.shouldLoad())
            return;

        activePermissionHooks.add(permissionHook);
    }

    public boolean isProtected(Player player, Location location) {
        boolean result = false;

        for (PermissionHook permissionHook : activePermissionHooks) {
            if (!permissionHook.isProtected(player, location))
                continue;

            result = true;
            break;
        }

        return result;
    }
}
