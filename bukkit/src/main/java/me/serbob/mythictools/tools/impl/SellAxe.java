package me.serbob.mythictools.tools.impl;

import me.serbob.mythictools.abilities.Abilities;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.mythictools.tools.AbstractTool;

public class SellAxe extends AbstractTool {
    public SellAxe() {
        super(ConfigSelector.SELLAXE, Abilities.SELL.getAbility());
    }
}
