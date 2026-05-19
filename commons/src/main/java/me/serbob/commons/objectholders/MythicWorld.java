package me.serbob.commons.objectholders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

@Getter
@AllArgsConstructor
public class MythicWorld {
    private final String worldName;

    public World adaptToWorld() {
        return Bukkit.getWorld(worldName);
    }
}
