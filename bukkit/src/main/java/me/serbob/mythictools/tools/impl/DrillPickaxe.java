package me.serbob.mythictools.tools.impl;

import me.serbob.mythictools.abilities.Abilities;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.mythictools.tools.AbstractTool;

public class DrillPickaxe extends AbstractTool {
    public DrillPickaxe() {
        super(ConfigSelector.DRILL, Abilities.DRILL.getAbility());
    }
}
