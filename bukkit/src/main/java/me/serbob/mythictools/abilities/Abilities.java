package me.serbob.mythictools.abilities;

import lombok.Getter;
import me.serbob.mythictools.abilities.impl.*;
import me.serbob.commons.utils.nbt.NBTUtils;
import org.bukkit.inventory.ItemStack;

public enum Abilities {
    DRILL(new DrillAbility()),
    DRAIN(new DrainAbility()),
    TREE_CHOPPER(new TreeChopperAbility()),
    SELL(new SellAbility()),
    MULTI_TOOL(new MultiToolAbility()),
    SHOVEL_3X3(new Shovel3x3Ability()),
    INFINITEFIREWORK(new InfiniteFireworkAbility()),
    ;

    @Getter private final AbstractAbility ability;

    Abilities(AbstractAbility ability) {
        this.ability = ability;
    }

    public static AbstractAbility getAbilityFromNBT(String nbt) {
        AbstractAbility ability = null;

        for (Abilities abilityEnum : values()) {
            if (!abilityEnum.getAbility().getNbt().equalsIgnoreCase(nbt))
                continue;

            ability = abilityEnum.getAbility();
            break;
        }

        return ability;
    }

    public static boolean addAbility(ItemStack itemStack, String nbt) {
        if (Abilities.getAbilityFromNBT(nbt) == null)
            return false;

        NBTUtils.getInstance().modifyNbt(itemStack, nbt);

        return true;
    }

    public static void addAbility(ItemStack itemStack, Abilities ability) {
        AbstractAbility abstractAbility = ability.getAbility();

        NBTUtils.getInstance().modifyNbt(itemStack, abstractAbility.getNbt());
    }

    public static AbstractAbility getByNbt(String nbt) {
        for (Abilities abilities : values()) {
            if (!abilities.getAbility().getNbt().equals(nbt))
                continue;

            return abilities.getAbility();
        }

        return null;
    }
}
