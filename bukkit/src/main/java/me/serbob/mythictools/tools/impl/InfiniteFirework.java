package me.serbob.mythictools.tools.impl;

import me.serbob.commons.enums.ConfigSelector;
import me.serbob.mythictools.abilities.Abilities;
import me.serbob.mythictools.tools.AbstractTool;

public class InfiniteFirework extends AbstractTool {
    public InfiniteFirework() {
        super(ConfigSelector.INFINITEFIREWORK, Abilities.INFINITEFIREWORK.getAbility());
    }
}

