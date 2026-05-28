package me.serbob.xaltools.tools.impl;

import me.serbob.xaltools.abilities.Abilities;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.xaltools.tools.AbstractTool;

public class Bucket extends AbstractTool {
    public Bucket() {
        super(ConfigSelector.BUCKET, Abilities.DRAIN.getAbility());
    }
}
