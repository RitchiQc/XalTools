package me.serbob.commons;

import com.tcoded.folialib.FoliaLib;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Commons {
    @Getter(lazy = true)
    private static final Commons instance = new Commons();

    private static @Getter JavaPlugin pluginInstance;
    private static @Getter FoliaLib foliaLib;

    public void load(
            JavaPlugin plugin
    ) {
        pluginInstance = plugin;
        foliaLib = new FoliaLib(plugin);
    }
}
