package me.serbob.xaltools.abilities.impl;

import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;

public enum MagnetMode {
    NORMAL("&aNormal"),
    FARM("&eFarm");

    private final String displayName;

    MagnetMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MagnetMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    private static final Set<Material> FARM_ITEMS = EnumSet.of(
            // === MINERAIS ===
            // Minerais bruts
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE,
            Material.ANCIENT_DEBRIS,
            // Items bruts
            Material.RAW_IRON, Material.RAW_COPPER, Material.RAW_GOLD,
            // Lingots / Gemmes / Poussières
            Material.COAL, Material.IRON_INGOT, Material.COPPER_INGOT,
            Material.GOLD_INGOT, Material.REDSTONE, Material.EMERALD,
            Material.LAPIS_LAZULI, Material.DIAMOND, Material.NETHERITE_INGOT,
            Material.NETHERITE_SCRAP, Material.QUARTZ, Material.AMETHYST_SHARD,
            Material.AMETHYST_CLUSTER, Material.BUDDING_AMETHYST,
            Material.SMALL_AMETHYST_BUD, Material.MEDIUM_AMETHYST_BUD,
            Material.LARGE_AMETHYST_BUD,

            // === BOIS ===
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG, Material.CHERRY_LOG, Material.PALE_OAK_LOG,
            Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG,
            Material.STRIPPED_BIRCH_LOG, Material.STRIPPED_JUNGLE_LOG,
            Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
            Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG,
            Material.STRIPPED_PALE_OAK_LOG,
            Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.BIRCH_WOOD,
            Material.JUNGLE_WOOD, Material.ACACIA_WOOD, Material.DARK_OAK_WOOD,
            Material.MANGROVE_WOOD, Material.CHERRY_WOOD, Material.PALE_OAK_WOOD,
            Material.STRIPPED_OAK_WOOD, Material.STRIPPED_SPRUCE_WOOD,
            Material.STRIPPED_BIRCH_WOOD, Material.STRIPPED_JUNGLE_WOOD,
            Material.STRIPPED_ACACIA_WOOD, Material.STRIPPED_DARK_OAK_WOOD,
            Material.STRIPPED_MANGROVE_WOOD, Material.STRIPPED_CHERRY_WOOD,
            Material.STRIPPED_PALE_OAK_WOOD,
            Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS,
            Material.JUNGLE_PLANKS, Material.ACACIA_PLANKS, Material.DARK_OAK_PLANKS,
            Material.MANGROVE_PLANKS, Material.CHERRY_PLANKS, Material.PALE_OAK_PLANKS,
            Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.BIRCH_SAPLING,
            Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING,
            Material.MANGROVE_PROPAGULE, Material.CHERRY_SAPLING, Material.PALE_OAK_SAPLING,
            Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES,
            Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES,
            Material.MANGROVE_LEAVES, Material.CHERRY_LEAVES, Material.PALE_OAK_LEAVES,
            Material.AZALEA_LEAVES, Material.FLOWERING_AZALEA_LEAVES,
            Material.STICK,

            // === FARM ===
            // Crops de base
            Material.WHEAT, Material.WHEAT_SEEDS,
            Material.CARROT,
            Material.POTATO,
            Material.BEETROOT, Material.BEETROOT_SEEDS,
            // Melon / Pumpkin
            Material.MELON, Material.MELON_SLICE, Material.MELON_SEEDS,
            Material.PUMPKIN, Material.PUMPKIN_SEEDS, Material.CARVED_PUMPKIN,
            Material.JACK_O_LANTERN,
            // Canne à sucre / Cactus / Bambou
            Material.SUGAR_CANE, Material.CACTUS, Material.BAMBOO,
            // Algues / Coraux
            Material.KELP, Material.SEAGRASS, Material.SEA_PICKLE,
            // Baies / Cocoa
            Material.COCOA_BEANS, Material.SWEET_BERRIES, Material.GLOW_BERRIES,
            // Champignons
            Material.BROWN_MUSHROOM, Material.RED_MUSHROOM,
            Material.CRIMSON_FUNGUS, Material.WARPED_FUNGUS,
            // Nether wart
            Material.NETHER_WART, Material.NETHER_WART_BLOCK, Material.WARPED_WART_BLOCK,
            // Fleurs (pour dyes)
            Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID,
            Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP,
            Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP,
            Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY,
            Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH, Material.PEONY,
            Material.TORCHFLOWER, Material.TORCHFLOWER_SEEDS,
            Material.PITCHER_PLANT, Material.PITCHER_POD,
            // Produits agricoles
            Material.EGG, Material.FEATHER, Material.LEATHER,
            Material.HAY_BLOCK, Material.BONE_MEAL, Material.BONE,
            // Nourriture de base
            Material.APPLE, Material.BREAD,
            Material.PORKCHOP, Material.COOKED_PORKCHOP,
            Material.BEEF, Material.COOKED_BEEF,
            Material.CHICKEN, Material.COOKED_CHICKEN,
            Material.MUTTON, Material.COOKED_MUTTON,
            Material.RABBIT, Material.COOKED_RABBIT, Material.RABBIT_STEW,
            Material.COD, Material.COOKED_COD, Material.SALMON, Material.COOKED_SALMON,
            Material.TROPICAL_FISH, Material.PUFFERFISH,
            Material.BAKED_POTATO, Material.POISONOUS_POTATO,
            Material.PUMPKIN_PIE, Material.COOKIE, Material.CAKE
    );

    public boolean isAllowed(Material material) {
        if (this == NORMAL) {
            return true;
        }
        return FARM_ITEMS.contains(material);
    }
}
