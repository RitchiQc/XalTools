package me.serbob.xaltools.tools.impl;

import me.serbob.xaltools.abilities.Abilities;
import me.serbob.xaltools.abilities.AbstractAbility;
import me.serbob.xaltools.tools.AbstractTool;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class DynamicTool extends AbstractTool {

    public DynamicTool(FileConfiguration config) {
        super(config, resolveAbilities(config));
    }

    private static AbstractAbility[] resolveAbilities(FileConfiguration config) {
        List<String> abilityNames = config.getStringList("abilities");
        List<AbstractAbility> abilities = new ArrayList<>();

        for (String abilityName : abilityNames) {
            AbstractAbility ability = Abilities.getByNbt(abilityName);
            if (ability != null) {
                abilities.add(ability);
            }
        }

        return abilities.toArray(new AbstractAbility[0]);
    }
}
