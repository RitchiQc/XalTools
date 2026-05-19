package me.serbob.mythictools.tools;

import me.serbob.mythictools.abilities.AbstractAbility;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.mythictools.utils.item.ItemUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractTool {
    protected final FileConfiguration config;
    private final AbstractAbility[] ability;

    public AbstractTool(ConfigSelector configSelector, AbstractAbility ability) {
        this.config = configSelector.getConfig();
        this.ability = new AbstractAbility[]{ability};
    }

    public AbstractTool(ConfigSelector configSelector, AbstractAbility[] ability) {
        this.config = configSelector.getConfig();
        this.ability = ability;
    }

    public AbstractTool(FileConfiguration config, AbstractAbility ability) {
        this.config = config;
        this.ability = new AbstractAbility[]{ability};
    }

    public AbstractTool(FileConfiguration config, AbstractAbility[] ability) {
        this.config = config;
        this.ability = ability;
    }

    public ItemStack parseItem() {
        return ItemUtil.parseItem(config, ability);
    }
}
