package me.serbob.mythictools.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.commons.objectholders.MythicWorld;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlacklistManager {
    @Getter(lazy = true)
    private static final BlacklistManager instance = new BlacklistManager();

    Set<MythicWorld> blacklistedWorlds = new HashSet<>();
    Set<Material> blacklistedBlocks  = new HashSet<>();

    public void load() {
        FileConfiguration config = ConfigSelector.BLACKLIST.getConfig();

        blacklistedWorlds.clear();
        blacklistedBlocks.clear();

        config.getStringList("worlds").forEach(world -> {
            blacklistedWorlds.add(new MythicWorld(world));
        });

        config.getStringList("blocks").forEach(material -> {
            Material blockMaterial = Material.matchMaterial(material);

            if (blockMaterial == null)
                return;

            blacklistedBlocks.add(blockMaterial);
        });
    }

    public boolean isBlacklistedWorld(World world) {
        boolean isBlacklisted = false;

        for (MythicWorld mythicWorld : blacklistedWorlds) {
            World parsedWorld = mythicWorld.adaptToWorld();

            if (parsedWorld == null)
                continue;

            if (!parsedWorld.getName().equals(world.getName()))
                continue;

            isBlacklisted = true;
        }

        return isBlacklisted;
    }

    public boolean isBlacklistedBlock(Block block) {
        return blacklistedBlocks.contains(block.getType());
    }
}
