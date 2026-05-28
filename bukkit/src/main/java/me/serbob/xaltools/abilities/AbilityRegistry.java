package me.serbob.xaltools.abilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
        return Abilities.getByNbt(nbt);
    }

    public List<AbstractAbility> getAllAbilities() {
        List<AbstractAbility> all = new ArrayList<>();

        for (Abilities ability : Abilities.values()) {
            all.add(ability.getAbility());
        }

        return all;
    }

    public List<String> getAllNbtTags() {
        List<String> tags = new ArrayList<>();

        for (Abilities ability : Abilities.values()) {
            tags.add(ability.getAbility().getNbt());
        }

        return tags;
    }
}
