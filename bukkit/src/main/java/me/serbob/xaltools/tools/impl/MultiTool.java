package me.serbob.xaltools.tools.impl;

import me.serbob.xaltools.abilities.Abilities;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.xaltools.tools.AbstractTool;

public class MultiTool extends AbstractTool {
    public MultiTool() {
        super(ConfigSelector.MULTITOOL, Abilities.MULTI_TOOL.getAbility());
    }
}
