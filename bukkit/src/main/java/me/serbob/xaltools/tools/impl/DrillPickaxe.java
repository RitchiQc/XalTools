package me.serbob.xaltools.tools.impl;

import me.serbob.xaltools.abilities.Abilities;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.xaltools.tools.AbstractTool;

public class DrillPickaxe extends AbstractTool {
    public DrillPickaxe() {
        super(ConfigSelector.DRILL, Abilities.DRILL.getAbility());
    }
}
