package me.serbob.xaltools.tools.impl;

import me.serbob.xaltools.abilities.Abilities;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.xaltools.tools.AbstractTool;

public class SellAxe extends AbstractTool {
    public SellAxe() {
        super(ConfigSelector.SELLAXE, Abilities.SELL.getAbility());
    }
}
