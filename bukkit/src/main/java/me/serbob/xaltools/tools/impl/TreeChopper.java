package me.serbob.xaltools.tools.impl;

import me.serbob.xaltools.abilities.Abilities;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.xaltools.tools.AbstractTool;

public class TreeChopper extends AbstractTool {
    public TreeChopper() {
        super(ConfigSelector.TREECHOPPER, Abilities.TREE_CHOPPER.getAbility());
    }
}
