package me.serbob.mythictools.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.serbob.commons.Commons;
import me.serbob.mythictools.tools.AbstractTool;
import me.serbob.mythictools.tools.Tools;
import me.serbob.mythictools.tools.impl.DynamicTool;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ToolManager {
    @Getter(lazy = true)
    private static final ToolManager instance = new ToolManager();

    @Getter Map<String, AbstractTool> tools = new HashMap<>();

    public void register() {
        tools.clear();

        registerDefaults();
        registerDynamicTools();
    }

    private void registerDefaults() {
        for (Tools tool : Tools.values()) {
            tools.put(tool.name().toLowerCase(), tool.getTool());
        }
    }

    private void registerDynamicTools() {
        File toolsFolder = new File(Commons.getPluginInstance().getDataFolder(), "tools");
        if (!toolsFolder.exists() || !toolsFolder.isDirectory()) {
            return;
        }

        File[] files = toolsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            String fileName = file.getName();
            String toolName = fileName.substring(0, fileName.length() - 4).toLowerCase();

            // Skip if already registered as a default tool
            if (tools.containsKey(toolName)) {
                continue;
            }

            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                DynamicTool dynamicTool = new DynamicTool(config);
                tools.put(toolName, dynamicTool);
                log.info("Registered dynamic tool: " + toolName);
            } catch (Exception e) {
                log.warn("Failed to load dynamic tool '" + toolName + "': " + e.getMessage());
            }
        }
    }
}
