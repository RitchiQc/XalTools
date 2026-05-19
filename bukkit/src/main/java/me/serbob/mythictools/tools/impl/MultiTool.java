package me.serbob.mythictools.tools.impl;

import me.serbob.mythictools.abilities.Abilities;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.mythictools.tools.AbstractTool;

public class MultiTool extends AbstractTool {
    public MultiTool() {
        super(ConfigSelector.MULTITOOL, Abilities.MULTI_TOOL.getAbility());
    }
}
