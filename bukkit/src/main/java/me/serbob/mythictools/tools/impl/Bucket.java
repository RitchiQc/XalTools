package me.serbob.mythictools.tools.impl;

import me.serbob.mythictools.abilities.Abilities;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.mythictools.tools.AbstractTool;

public class Bucket extends AbstractTool {
    public Bucket() {
        super(ConfigSelector.BUCKET, Abilities.DRAIN.getAbility());
    }
}
