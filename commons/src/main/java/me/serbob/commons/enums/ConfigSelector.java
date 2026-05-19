package me.serbob.commons.enums;

import me.serbob.commons.Commons;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public enum ConfigSelector {
    CONFIG("config.yml"),
    HOOKS("hooks.yml"),
    SELFDESTRUCT("selfdestruct.yml"),
    MESSAGES("messages.yml"),
    BLACKLIST("blacklist.yml"),

    /*
     * Tools
     */
    DRILL("tools/drill.yml"),
    BUCKET("tools/bucket.yml"),
    SHOVEL("tools/shovel.yml"),
    TREECHOPPER("tools/treechopper.yml"),
    MULTITOOL("tools/multitool.yml"),
    SELLAXE("tools/sellaxe.yml"),
    INFINITEFIREWORK("tools/infinitefirework.yml"),
    ;

    private final String fileName;
    private File file;
    private FileConfiguration config;

    ConfigSelector(String fileName) {
        this.fileName = fileName;
    }

    public void initialize() {
        file = new File(Commons.getPluginInstance().getDataFolder(), fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            Commons.getPluginInstance().saveResource(fileName, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            throw new IllegalStateException("Config " + fileName + " has not been initialized. Call initialize() first.");
        }
        return config;
    }

    public void saveConfig() {
        if (config == null || file == null) {
            throw new IllegalStateException("Config " + fileName + " has not been initialized. Call initialize() first.");
        }
        try {
            getConfig().save(file);
            getConfig().load(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void reload() {
        if (file == null) {
            throw new IllegalStateException("Config " + fileName + " has not been initialized. Call initialize() first.");
        }
        config = YamlConfiguration.loadConfiguration(file);
        try {
            config.save(file);
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void appendToConfig(String path, Object value) {
        if (config.contains(path))
            return;

        if (config == null || file == null) {
            throw new IllegalStateException("Config " + fileName + " has not been initialized. Call initialize() first.");
        }

        config.set(path, value);

        saveConfig();
    }
}
