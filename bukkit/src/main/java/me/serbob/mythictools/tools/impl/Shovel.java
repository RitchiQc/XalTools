package me.serbob.mythictools.tools.impl;

import me.serbob.mythictools.abilities.Abilities;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.mythictools.tools.AbstractTool;

public class Shovel extends AbstractTool {
    public Shovel() {
        super(ConfigSelector.SHOVEL, Abilities.SHOVEL_3X3.getAbility());
    }
}
