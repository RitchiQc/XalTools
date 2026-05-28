package me.serbob.xaltools.tools.impl;

import me.serbob.commons.enums.ConfigSelector;
import me.serbob.xaltools.abilities.Abilities;
import me.serbob.xaltools.tools.AbstractTool;

public class InfiniteFirework extends AbstractTool {
    public InfiniteFirework() {
        super(ConfigSelector.INFINITEFIREWORK, Abilities.INFINITEFIREWORK.getAbility());
    }
}

