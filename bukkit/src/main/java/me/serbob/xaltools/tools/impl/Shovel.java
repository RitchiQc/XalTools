package me.serbob.xaltools.tools.impl;

import me.serbob.xaltools.abilities.Abilities;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.xaltools.tools.AbstractTool;

public class Shovel extends AbstractTool {
    public Shovel() {
        super(ConfigSelector.SHOVEL, Abilities.SHOVEL_3X3.getAbility());
    }
}
