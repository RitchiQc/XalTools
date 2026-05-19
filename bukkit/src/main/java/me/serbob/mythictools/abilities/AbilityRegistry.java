package me.serbob.mythictools.abilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.serbob.mythictools.abilities.custom.CustomAbility;
import me.serbob.mythictools.abilities.custom.CustomAbilityManager;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AbilityRegistry {

    private static AbilityRegistry instance;

    public static AbilityRegistry getInstance() {
        if (instance == null) {
            instance = new AbilityRegistry();
        }
        return instance;
    }

    public AbstractAbility getByNbt(String nbt) {
        // Check hardcoded abilities first
        AbstractAbility ability = Abilities.getByNbt(nbt);
        if (ability != null) {
            return ability;
        }

        // Check custom abilities
        return CustomAbilityManager.getInstance().getByNbt(nbt);
    }

    public List<AbstractAbility> getAllAbilities() {
        List<AbstractAbility> all = new ArrayList<>();

        for (Abilities ability : Abilities.values()) {
            all.add(ability.getAbility());
        }

        all.addAll(CustomAbilityManager.getInstance().getAllAbilities());

        return all;
    }

    public List<String> getAllNbtTags() {
        List<String> tags = new ArrayList<>();

        for (Abilities ability : Abilities.values()) {
            tags.add(ability.getAbility().getNbt());
        }

        for (CustomAbility ability : CustomAbilityManager.getInstance().getAllAbilities()) {
            tags.add(ability.getNbt());
        }

        return tags;
    }
}
