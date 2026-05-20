package me.serbob.mythictools.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.serbob.mythictools.tools.Tools;

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
    }

    private void registerDefaults() {
        for (Tools tool : Tools.values()) {
            tools.put(tool.name().toLowerCase(), tool.getTool());
        }
    }
}
