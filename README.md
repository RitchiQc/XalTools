# MythicTools

Advanced tools plugin for **Folia** and **Paper** Minecraft servers.

## Requirements

- **Java 21** (minimum)
- **Folia 1.21.11+** or **Paper 1.21.4+**
- **Maven 3.9+** (for building)

## Build

```bash
mvn clean package
```

The final shaded JAR will be located at:
```
dist/target/MythicTools-<version>.jar
```

## Project Structure

| Module | Description |
|--------|-------------|
| `commons` | Shared utilities and common code |
| `api` | Public API for developers |
| `bukkit` | Main Bukkit/Folia plugin module |
| `hooks_java17` | Hooks for WorldGuard, Lands and other plugins |
| `dist` | Assembly module (produces the final shaded JAR) |

## Dependencies

- [Paper API](https://github.com/PaperMC/Paper)
- [FoliaLib](https://github.com/TechnicallyCoded/FoliaLib)
- [Item-NBT-API](https://github.com/tr7zw/Item-NBT-API)
- [VaultAPI](https://github.com/MilkBowl/VaultAPI)
- [ShopGUI+ API](https://github.com/brcdev-minecraft/shopgui-api)
- [LandsAPI](https://github.com/Angeschossen/LandsAPI)
- [WorldGuard](https://github.com/EngineHub/WorldGuard)

## Features

- Custom tools with special abilities
- **Custom abilities** — create your own abilities via YAML!
- Folia-supported (async region scheduling)
- Economy integration (Vault)
- Shop integration (ShopGUI+, EconomyShopGUI)
- Land protection hooks (WorldGuard, Lands)
- Self-destruct timer on items
- Highly configurable via YAML

## Available Abilities

### Hardcoded Abilities

| Ability | NBT Tag | Description |
|---------|---------|-------------|
| Amethyst Bucket | `amethyst_bucket` | Drains water/lava in a 3x3 area |
| Drill Pickaxe | `drill_pickaxe` | Breaks 9 blocks at once (3x3) |
| Amethyst Multitool | `amethyst_multitool` | Auto-switches tool type based on target |
| Sell Axe | `sell_axe` | Sells chest contents on break |
| Amethyst Shovel | `amethyst_shovel` | Breaks 9 blocks at once (3x3) |
| Amethyst Axe | `amethyst_axe` | Chops entire trees at once |
| Infinite Firework | `infinite_firework` | Infinite elytra boost fireworks |
| No Fall Damage | `no_fall_damage` | Prevents fall damage when equipped |
| Haste | `haste` | Haste potion effect (configurable amplifier) |
| Speed | `speed` | Speed potion effect (configurable amplifier) |
| Fire Resistance | `fire_resistance` | Fire resistance potion effect |
| Jump Boost | `jump_boost` | Jump boost potion effect |
| Night Vision | `night_vision` | Night vision potion effect |
| Water Breathing | `water_breathing` | Water breathing potion effect |
| Regeneration | `regeneration` | Regeneration potion effect |
| Strength | `strength` | Strength potion effect |
| Invisibility | `invisibility` | Invisibility potion effect |

### Potion Effect Amplifier

For hardcoded potion effect abilities (Haste, Speed, etc.), the amplifier is configured directly on the item via `potion_amplifier` in the tool's YAML file:

```yaml
item:
  material: NETHERITE_PICKAXE
  name: '#A303F9ᴄᴜsᴛᴏᴍ ᴀᴍᴇᴛʜʏsᴛ ᴛᴏᴏʟ'
  lore:
  - '&6Haste 3'
  enchants:
  - "MENDING:1"
  - "UNBREAKING:3"
  - "EFFICIENCY:5"

abilities:
- haste

potion_amplifier: 3  # Effect level (0 = I, 1 = II, 2 = III, etc.)
```

The `potion_amplifier` value is stored in the item's NBT and read dynamically when the effect is applied.

### Custom Abilities (create your own!)

Create YAML files in the `custom_abilities/` folder. Available types:

| Type | Description | Slot |
|------|-------------|------|
| `NO_FALL_DAMAGE` | Prevents fall damage | `FEET` (boots) |
| `FIRE_RESISTANCE` | Fire resistance potion effect | Any armor |
| `SPEED` | Speed potion effect | Any armor |
| `JUMP_BOOST` | Jump boost potion effect | Any armor |
| `NIGHT_VISION` | Night vision potion effect | Any armor |
| `WATER_BREATHING` | Water breathing potion effect | Any armor |
| `REGENERATION` | Regeneration potion effect | Any armor |
| `STRENGTH` | Strength potion effect | Any armor |
| `HASTE` | Haste potion effect | Any armor |
| `INVISIBILITY` | Invisibility potion effect | Any armor |

#### Custom Ability Example (`custom_abilities/no_fall.yml`)

```yaml
name: "No Fall Damage"
nbt: "no_fall_damage"
type: "NO_FALL_DAMAGE"
slot: "FEET"
```

#### Equipment Slots

- `HAND` — Must be held in main hand
- `FEET` — Must be equipped as boots
- `LEGS` — Must be equipped as leggings
- `CHEST` — Must be equipped as chestplate
- `HEAD` — Must be equipped as helmet
- `ANY_ARMOR` — Can be equipped on any armor slot

## License

All rights reserved.
