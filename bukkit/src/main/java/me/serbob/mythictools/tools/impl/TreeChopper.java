package me.serbob.mythictools.tools.impl;

import me.serbob.mythictools.abilities.Abilities;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.mythictools.tools.AbstractTool;

public class TreeChopper extends AbstractTool {
    public TreeChopper() {
        super(ConfigSelector.TREECHOPPER, Abilities.TREE_CHOPPER.getAbility());
    }
}
