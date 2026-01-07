# Auroral

> *"The sky gives, the ice remembers."*

A comprehensive winter expansion mod for Minecraft 1.21.11 (NeoForge 21.11.0-beta) centered around celestial energy, mystical metallurgy, and specialized survival.

## Overview

Auroral transforms cold biomes from barren wastes into hubs of high-tier progression. By harnessing the Aurora event, players can forge Shimmersteel, brew sub-zero concoctions, and equip gear that makes them masters of the frost.

## Features

### The Aurora (Celestial Engine)
- **33% chance** every night in cold biomes
- Atmospheric sky ribbons of neon green and pink
- Light levels never drop to total darkness during an Aurora
- **Celestial Repair**: Equipped Shimmersteel tools and Shimmerweave armor slowly repair automatically

### Glacial Basin
Your primary workstation for celestial crafting:
- Collects **Liquid Aura** during Aurora events
- Infuse materials by right-clicking:
  - Iron Ingot → **Shimmersteel Ingot**
  - Leather/Wool → **Shimmerweave Fabric**

### Shimmersteel Armory
Tools slightly better than Iron with significantly higher enchantability:

| Tool | Special Ability |
|------|-----------------|
| **Pickaxe** | Inherent Fortune III for gems (Diamond, Emerald, Lapis, etc.) |
| **Axe** | Right-click copper to force oxidation |
| **Shovel** | Inherent Silk Touch for all blocks |
| **Sword** | Applies Slowness I; **Execute** at <15% HP (leaves decorative snow pile) |
| **Bow** | Uses Snowballs as ammo → Star-Shots (damage + flashbang burst) |

### Shimmerweave Armor

| Piece | Special Ability |
|-------|-----------------|
| **Goggles** | Applies Glowing to hostiles in 32-block radius |
| **Tunic** | Automatically extinguishes fire |
| **Leggings** | Speed buff on snow; Soul Speed on Soul Sand |
| **Skates** | Speed I on ice/snow, zero friction, Frost Walker, Lava→Obsidian, fall immunity on ice/obsidian |

### Cold Brewing Stand
Bypass the Nether for your alchemical needs:
- Crafted with Shimmersteel and Blue Ice
- Uses **Snowballs** instead of Blaze Powder
- Functions exactly like vanilla brewing

### Special Blocks

- **Hearthwood Log**: Burns for 7 days, grants frostbite immunity, villager discounts, and can catalyze an Aurora
- **Shimmering Ice**: Glowing ice that never freezes and hydrates farmland
- **Glow-Leeks**: Grown on Shimmering Ice; grants Night Vision and Glowing
- **Aurora Blooms**: Translucent flowers that spawn during Auroras, drop Frozen Petals

## Requirements

- Minecraft 1.21.11
- NeoForge 21.11.0-beta+
- Java 21

## Optional Dependencies

- [Jade](https://www.curseforge.com/minecraft/mc-mods/jade) - Shows Aurora status and block information tooltips
- [GuideMe](https://www.curseforge.com/minecraft/mc-mods/guideme) - In-game guidebook for Auroral mechanics

## Installation

1. Install NeoForge 21.11.0-beta+ for Minecraft 1.21.11
2. Download the latest Auroral JAR
3. Place in your `mods/` folder
4. Launch Minecraft

## Configuration

Server and client configuration files are generated on first run:
- `config/auroral-server.toml`
- `config/auroral-client.toml`

### Key Server Options
| Option | Default | Description |
|--------|---------|-------------|
| `auroraChance` | 0.33 | Chance of Aurora each night (0.0-1.0) |
| `auroraRepairRate` | 1 | Durability restored per second during Aurora |
| `executeThreshold` | 0.15 | HP percentage for Execute mechanic |
| `glowingRadius` | 32 | Radius for Goggles' Glowing effect |

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## Credits

- **Author**: Saereth
- **Minecraft Version**: 1.21.11
- **Mod Loader**: NeoForge 21.11.0-beta

---

*"The sky gives, the ice remembers."*
