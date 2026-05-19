package me.serbob.mythictools.abilities.custom;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.serbob.commons.Commons;
import me.serbob.commons.enums.DirectorySelector;
import me.serbob.mythictools.MythicTools;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomAbilityManager {
    @Getter(lazy = true)
    private static final CustomAbilityManager instance = new CustomAbilityManager();

    @Getter
    private final Map<String, CustomAbility> customAbilities = new HashMap<>();

    public void load() {
        customAbilities.clear();

        DirectorySelector.CUSTOM_ABILITIES.getAllConfigurations()
                .forEach((fileName, config) -> {
                    try {
                        CustomAbility ability = parseAbility(config);
                        if (ability != null) {
                            customAbilities.put(ability.getNbt(), ability);
                            Bukkit.getPluginManager().registerEvents(ability, MythicTools.getInstance());
                            log.info("Loaded custom ability: {} (NBT: {}, Type: {})",
                                    ability.getDisplayName(), ability.getNbt(), ability.getType().name());
                        }
                    } catch (Exception e) {
                        log.error("Failed to load custom ability from file: {}", fileName, e);
                    }
                });

        startEffectChecker();
    }

    private CustomAbility parseAbility(FileConfiguration config) {
        String nbt = config.getString("nbt");
        String typeStr = config.getString("type");
        String name = config.getString("name", "Custom Ability");
        String slotStr = config.getString("slot", "HAND");
        int potionDuration = config.getInt("potion-duration", 200);
        int potionAmplifier = config.getInt("potion-amplifier", 0);

        if (nbt == null || typeStr == null) {
            log.warn("Custom ability missing 'nbt' or 'type' field");
            return null;
        }

        CustomAbilityType type;
        try {
            type = CustomAbilityType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown custom ability type: {}", typeStr);
            return null;
        }

        CustomAbility.EquipmentSlot slot;
        try {
            slot = CustomAbility.EquipmentSlot.valueOf(slotStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown equipment slot: {}, defaulting to HAND", slotStr);
            slot = CustomAbility.EquipmentSlot.HAND;
        }

        return new CustomAbility(nbt, type, name, potionDuration, potionAmplifier, slot);
    }

    public CustomAbility getByNbt(String nbt) {
        return customAbilities.get(nbt);
    }

    public List<CustomAbility> getAllAbilities() {
        return new ArrayList<>(customAbilities.values());
    }

    private void startEffectChecker() {
        Commons.getFoliaLib().getScheduler().runTimerAsync(task -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                for (CustomAbility ability : customAbilities.values()) {
                    ability.checkAndApplyEffect(player);
                }
            });
        }, 20L, 20L);
    }
}
