package me.serbob.commons.enums;

import me.serbob.commons.Commons;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

public enum DirectorySelector {
    CUSTOM_TOOLS("custom_tools")
    ;

    private final String directoryName;
    private Map<String, FileConfiguration> configCache;

    DirectorySelector(String directoryName) {
        this.directoryName = directoryName;
        this.configCache = new HashMap<>();
    }

    public void initialize() {
        configCache.clear();
        JavaPlugin plugin = Commons.getPluginInstance();
        File dataFolder = plugin.getDataFolder();
        File directory = new File(dataFolder, directoryName);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        copyAllYamlFilesFromResources(plugin, directory);

        loadAllYamlFiles(directory);
    }

    public List<File> getAllYamlFiles() {
        File directory = new File(Commons.getPluginInstance().getDataFolder(), directoryName);
        List<File> yamlFiles = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
            if (files != null) {
                yamlFiles.addAll(Arrays.asList(files));
            }
        }

        return yamlFiles;
    }

    private void copyAllYamlFilesFromResources(JavaPlugin plugin, File directory) {
        try {
            URI uri = plugin.getClass().getResource("/" + directoryName).toURI();
            Path myPath;

            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                myPath = fileSystem.getPath("/" + directoryName);
            } else {
                myPath = Paths.get(uri);
            }

            Files.walk(myPath, 1)
                    .filter(path -> path.toString().endsWith(".yml"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        File outFile = new File(directory, fileName);

                        if (!outFile.exists()) {
                            plugin.saveResource(directoryName + "/" + fileName, false);
                        }
                    });

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to copy files: " + e.getMessage());
        }
    }

    public void reload() {
        JavaPlugin plugin = Commons.getPluginInstance();
        File dataFolder = plugin.getDataFolder();
        File directory = new File(dataFolder, directoryName);

        configCache.clear();

        loadAllYamlFiles(directory);
    }

    private void loadAllYamlFiles(File directory) {
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                try {
                    config.save(file);
                    config.load(file);
                } catch (IOException | InvalidConfigurationException e) {
                    throw new RuntimeException(e);
                }
                configCache.put(file.getName(), config);
            }
        }
    }

    public FileConfiguration getFileConfiguration(String fileName) {
        if (!fileName.endsWith(".yml")) {
            fileName += ".yml";
        }
        return configCache.get(fileName);
    }

    public Map<String, FileConfiguration> getAllConfigurations() {
        return new HashMap<>(configCache);
    }

    public boolean doesConfigurationExist(String fileName) {
        if (!fileName.endsWith(".yml")) {
            fileName += ".yml";
        }
        return configCache.containsKey(fileName);
    }

    public File getDataFolder() {
        return new File(Commons.getPluginInstance().getDataFolder(), directoryName);
    }
}
