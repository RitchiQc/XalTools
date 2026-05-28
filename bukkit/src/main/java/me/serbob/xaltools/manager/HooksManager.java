package me.serbob.xaltools.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.serbob.commons.enums.ConfigSelector;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HooksManager {
    @Getter(lazy = true)
    private static final HooksManager instance = new HooksManager();

    private String currentCurrency;
    private String currentShop;

    public void load() {
        FileConfiguration config = ConfigSelector.HOOKS.getConfig();

        currentCurrency = config.getString("currency");
        currentShop = config.getString("shop", "None");

        ConfigSelector.HOOKS.appendToConfig("hooks", new ArrayList<String>());
    }
}
