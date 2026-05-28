package me.serbob.xaltools.abilities;

import lombok.Getter;
import me.serbob.xaltools.abilities.impl.*;
import me.serbob.commons.utils.nbt.NBTUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public enum Abilities {
    DRILL(new DrillAbility()),
    DRAIN(new DrainAbility()),
    TREE_CHOPPER(new TreeChopperAbility()),
    SELL(new SellAbility()),
    MULTI_TOOL(new MultiToolAbility()),
    SHOVEL_3X3(new Shovel3x3Ability()),
    INFINITEFIREWORK(new InfiniteFireworkAbility()),
    NO_FALL_DAMAGE(new NoFallDamageAbility("no_fall_damage", NoFallDamageAbility.EquipmentSlot.FEET)),

    // Potion effect abilities
    HASTE(new PotionEffectAbility("haste", PotionEffectType.HASTE, PotionEffectAbility.EquipmentSlot.HAND)),
    SPEED(new PotionEffectAbility("speed", PotionEffectType.SPEED, PotionEffectAbility.EquipmentSlot.ANY_ARMOR)),
    FIRE_RESISTANCE(new PotionEffectAbility("fire_resistance", PotionEffectType.FIRE_RESISTANCE, PotionEffectAbility.EquipmentSlot.CHEST)),
    JUMP_BOOST(new PotionEffectAbility("jump_boost", PotionEffectType.JUMP_BOOST, PotionEffectAbility.EquipmentSlot.LEGS)),
    NIGHT_VISION(new PotionEffectAbility("night_vision", PotionEffectType.NIGHT_VISION, PotionEffectAbility.EquipmentSlot.HEAD)),
    WATER_BREATHING(new PotionEffectAbility("water_breathing", PotionEffectType.WATER_BREATHING, PotionEffectAbility.EquipmentSlot.HEAD)),
    REGENERATION(new PotionEffectAbility("regeneration", PotionEffectType.REGENERATION, PotionEffectAbility.EquipmentSlot.CHEST)),
    STRENGTH(new PotionEffectAbility("strength", PotionEffectType.STRENGTH, PotionEffectAbility.EquipmentSlot.HAND)),
    INVISIBILITY(new PotionEffectAbility("invisibility", PotionEffectType.INVISIBILITY, PotionEffectAbility.EquipmentSlot.CHEST)),
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
