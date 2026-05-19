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
- Folia-supported (async region scheduling)
- Economy integration (Vault)
- Shop integration (ShopGUI+, EconomyShopGUI)
- Land protection hooks (WorldGuard, Lands)
- Self-destruct timer on items
- Highly configurable via YAML

## License

All rights reserved.
