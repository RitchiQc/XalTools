package me.serbob.xaltools.tools.impl;

import me.serbob.xaltools.abilities.Abilities;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.xaltools.tools.AbstractTool;

public class Magnet extends AbstractTool {
    public Magnet() {
        super(ConfigSelector.MAGNET, Abilities.MAGNET.getAbility());
    }
}
