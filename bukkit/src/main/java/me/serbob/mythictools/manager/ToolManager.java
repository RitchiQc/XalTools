package me.serbob.mythictools.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.serbob.commons.enums.DirectorySelector;
import me.serbob.mythictools.abilities.AbilityRegistry;
import me.serbob.mythictools.abilities.AbstractAbility;
import me.serbob.mythictools.tools.AbstractTool;
import me.serbob.mythictools.tools.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        DirectorySelector.CUSTOM_TOOLS.getAllConfigurations()
                .forEach((fileName, config) -> {
                    List<String> abilities = config.getStringList("abilities");

                    if (abilities.isEmpty())
                        return;

                    List<AbstractAbility> abilitiesList = new ArrayList<>();
                    for (String nbtAbility : abilities) {
                        AbstractAbility ability = AbilityRegistry.getInstance().getByNbt(nbtAbility);

                        if (ability == null)
                            continue;

                        abilitiesList.add(ability);
                    }

                    AbstractTool tool = new AbstractTool(
                            config,
                            abilitiesList.toArray(new AbstractAbility[0])
                    ) {};

                    String toolName = fileName.toLowerCase().replace(".yml", "");
                    tools.put(toolName, tool);
                });
    }

    private void registerDefaults() {
        for (Tools tool : Tools.values()) {
            tools.put(tool.name().toLowerCase(), tool.getTool());
        }
    }
}
