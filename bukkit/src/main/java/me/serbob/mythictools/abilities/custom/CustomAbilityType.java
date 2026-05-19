package me.serbob.mythictools.abilities.custom;

import lombok.Getter;
import org.bukkit.potion.PotionEffectType;

public enum CustomAbilityType {
    NO_FALL_DAMAGE("Annule les dégâts de chute", null),
    FIRE_RESISTANCE("Résistance au feu", PotionEffectType.FIRE_RESISTANCE),
    SPEED("Vitesse augmentée", PotionEffectType.SPEED),
    JUMP_BOOST("Saut augmenté", PotionEffectType.JUMP_BOOST),
    NIGHT_VISION("Vision nocturne", PotionEffectType.NIGHT_VISION),
    WATER_BREATHING("Respiration aquatique", PotionEffectType.WATER_BREATHING),
    REGENERATION("Régénération", PotionEffectType.REGENERATION),
    STRENGTH("Force augmentée", PotionEffectType.STRENGTH),
    HASTE("Hâte", PotionEffectType.HASTE),
    INVISIBILITY("Invisibilité", PotionEffectType.INVISIBILITY),
    ;

    @Getter private final String description;
    @Getter private final PotionEffectType potionEffectType;

    CustomAbilityType(String description, PotionEffectType potionEffectType) {
        this.description = description;
        this.potionEffectType = potionEffectType;
    }

    public boolean isPotionEffect() {
        return potionEffectType != null;
    }

    public boolean handlesFallDamage() {
        return this == NO_FALL_DAMAGE;
    }
}
