# Auroral Implementation Plan

> "The sky gives, the ice remembers."

This document outlines the phased implementation approach for Auroral, a comprehensive winter expansion mod for Minecraft 1.21.1 (NeoForge).

---

## Phase 1: Foundation & Core Infrastructure

**Goal**: Establish the mod's skeleton, registry systems, and the Aurora event system.

### 1.1 Project Structure Setup
- [ ] Create package structure (`registry/`, `block/`, `item/`, `events/`, `client/`, `config/`, `data/`, `net/`, `util/`)
- [ ] Set up `ModBlocks.java` with DeferredRegister.createBlocks()
- [ ] Set up `ModItems.java` with DeferredRegister.createItems()
- [ ] Set up `ModBlockEntities.java`
- [ ] Set up `ModEntities.java`
- [ ] Set up `ModDataAttachments.java`
- [ ] Set up `ModCreativeTabs.java`
- [ ] Update `Auroral.java` to register all deferred registers

### 1.2 Configuration System
- [ ] Create `AuroralConfig.java` with server and client specs
- [ ] Server config: `auroraChance`, `auroraRepairRate`, `executeThreshold`, `hearthwoodLogBurnTime`, `glowingRadius`
- [ ] Client config: `showAuroraParticles`, `auroraIntensity`
- [ ] Register configs in main mod class

### 1.3 Aurora Event System (The Celestial Engine)
- [ ] Create `AuroraState` record with Codec for serialization
- [ ] Register `AURORA_STATE` level data attachment in `ModDataAttachments`
- [ ] Create `BiomeHelper.java` utility to detect cold biomes (uses biome tags)
- [ ] Create `AuroraHelper.java` utility for aurora state queries
- [ ] Create `AuroraEventHandler.java`:
  - Check for night transition each tick
  - Roll 33% chance for aurora in cold biome dimensions
  - Store aurora state in level attachment
  - Handle aurora end at dawn
- [ ] Create `SyncAuroraPayload.java` for client sync
- [ ] Set up `AuroralNetworking.java` to register payloads
- [ ] Sync aurora state to clients when it changes

### 1.4 Basic Materials (No Special Abilities Yet)
- [ ] Register `SHIMMERSTEEL_INGOT` item
- [ ] Register `SHIMMERWEAVE_FABRIC` item
- [ ] Register `AURORA_SHARD` item
- [ ] Register `FROZEN_PETALS` item
- [ ] Create basic item models and placeholder textures
- [ ] Add items to creative tab

**Verification**: Run client, confirm aurora triggers at night in snowy biome with 33% chance.

---

## Phase 2: Glacial Basin & Infusion System

**Goal**: Implement the primary workstation for material transformation.

### 2.1 Glacial Basin Block
- [ ] Create `GlacialBasinBlock.java` extending Block
  - Custom shape (cauldron-like)
  - BlockState property for fill level (0-3)
- [ ] Create `GlacialBasinBlockEntity.java`:
  - Store aura fill level
  - Tick method: increment fill during aurora
  - Save/load NBT for fill level
- [ ] Register block, block entity, and block item
- [ ] Create block model with fill-level variants
- [ ] Add crafting recipe: Stone + Blue Ice

### 2.2 Infusion Mechanics
- [ ] Implement `useItemOn()` in GlacialBasinBlock:
  - Iron Ingot → Shimmersteel Ingot (consume 1 aura level)
  - Leather → Shimmerweave Fabric (consume 1 aura level)
  - Wool → Shimmerweave Fabric (consume 1 aura level)
- [ ] Add particle effects for successful infusion
- [ ] Add sound effects for infusion
- [ ] Handle empty hand interaction (show fill level in chat/actionbar)

### 2.3 Basin Rendering (Client)
- [ ] Create `GlacialBasinRenderer.java` BlockEntityRenderer
- [ ] Render glowing liquid aura based on fill level
- [ ] Register renderer in `AuroralClient.java`

**Verification**: Place basin in snowy biome, wait for aurora, infuse iron to get shimmersteel.

---

## Phase 3: Shimmersteel Tools

**Goal**: Implement all Shimmersteel tools with their unique abilities.

### 3.1 Tool Tier Definition
- [ ] Create `ModTiers.java` with SHIMMERSTEEL tier:
  - Durability: 300 (slightly above iron's 250)
  - Speed: 6.5f (iron is 6.0f)
  - Damage: 2.5f (iron is 2.0f)
  - Enchantability: 22 (high, iron is 14)
  - Repair ingredient: Shimmersteel Ingot

### 3.2 Shimmersteel Pickaxe
- [ ] Create `ShimmersteelPickaxeItem.java`
- [ ] Override `mineBlock()` to apply Fortune III bonus for gems only:
  - Diamond Ore, Emerald Ore, Lapis Ore, Redstone Ore, Amethyst, Nether Quartz
  - Use loot modification or direct drop calculation
- [ ] Register item and model

### 3.3 Shimmersteel Axe
- [ ] Create `ShimmersteelAxeItem.java`
- [ ] Override `useOn()` for copper oxidation:
  - Check if target is WeatheringCopper
  - Force to next oxidation stage
  - Play sound and particles
- [ ] Register item and model

### 3.4 Shimmersteel Shovel
- [ ] Create `ShimmersteelShovelItem.java`
- [ ] Implement inherent Silk Touch:
  - Override loot context or use LootModifier
  - Apply silk touch behavior to all blocks
- [ ] Register item and model

### 3.5 Shimmersteel Sword
- [ ] Create `ShimmersteelSwordItem.java`
- [ ] Create `ShimmersteelEventHandler.java` for sword events:
  - `LivingHurtEvent`: Apply Slowness I on hit
  - `LivingHurtEvent`: Check if target < 15% HP, execute
  - `LivingDeathEvent`: Place snow layer on executed target position
- [ ] Register item and model

### 3.6 Shimmersteel Bow
- [ ] Create `ShimmersteelBowItem.java`:
  - Override `use()` to check for snowballs instead of arrows
  - Consume snowballs as ammo
- [ ] Create `StarShotEntity.java` projectile:
  - High velocity
  - On impact: deal damage + create light burst (flashbang effect)
  - Apply Blindness effect to nearby entities briefly
- [ ] Create `StarShotRenderer.java` for entity rendering
- [ ] Register entity type and item

### 3.7 Tool Recipes
- [ ] Create shaped recipes for all Shimmersteel tools
- [ ] Standard tool patterns using Shimmersteel Ingots + Sticks

**Verification**: Craft all tools, test each unique ability.

---

## Phase 4: Shimmerweave Armor

**Goal**: Implement all armor pieces with their unique effects.

### 4.1 Armor Material Definition
- [ ] Create `ModArmorMaterials.java` with SHIMMERWEAVE:
  - Defense values slightly above iron
  - High enchantability
  - Repair ingredient: Shimmerweave Fabric

### 4.2 Shimmersteel Goggles
- [ ] Create `ShimmersteelGogglesItem.java`:
  - Wearable in head slot
  - On tick: find all hostile entities in 32-block radius
  - Apply Glowing effect to found hostiles
- [ ] Optional: Curios integration for accessory slot
- [ ] Create `CuriosIntegration.java` if Curios is loaded
- [ ] Register item with special model (not full helmet)

### 4.3 Shimmerweave Tunic
- [ ] Create `ShimmerweaveTunicItem.java`
- [ ] In armor tick event or item tick:
  - Check if player is on fire
  - Extinguish fire instantly
- [ ] Register item and model

### 4.4 Shimmerweave Leggings
- [ ] Create `ShimmerweaveLeggingsItem.java`
- [ ] In player tick event:
  - Check block below player
  - If snow/snow block: apply Speed boost
  - If soul sand/soul soil: apply Soul Speed effect
- [ ] Register item and model

### 4.5 Shimmerweave Skates
- [ ] Create `ShimmerweaveSkatesItem.java`
- [ ] Implement multiple effects in player tick:
  - **Speed on ice/snow**: Apply Speed I when on ice or snow
  - **Zero friction**: Modify player slipperiness on ice
  - **Frost Walker**: Copy/extend FrostWalker enchantment logic for water→ice
  - **Lava Walker**: Check for lava below, replace with obsidian
  - **Fall immunity**: Cancel fall damage if landing on ice or obsidian
- [ ] Register item and model

### 4.6 Armor Recipes
- [ ] Create shaped recipes for all armor pieces
- [ ] Goggles: Shimmerweave + Shimmersteel pattern
- [ ] Tunic/Leggings/Skates: Standard armor patterns with Shimmerweave

### 4.7 Aurora Repair System
- [ ] In `AuroraEventHandler.java`, add player tick logic:
  - Check if aurora is active
  - Check if player is in cold biome
  - For each equipped Shimmersteel/Shimmerweave item:
    - Repair 1 durability per second (configurable)

**Verification**: Craft all armor, test each unique effect, verify aurora repair.

---

## Phase 5: Cold Brewing Stand

**Goal**: Implement the Nether-free alchemy workstation.

### 5.1 Cold Brewing Stand Block
- [ ] Create `ColdBrewingStandBlock.java`:
  - Similar to vanilla BrewingStandBlock
  - Custom block states for bottle slots
- [ ] Create `ColdBrewingStandBlockEntity.java`:
  - Mirror vanilla BrewingStandBlockEntity logic
  - Replace Blaze Powder fuel check with Snowball check
  - Same brewing recipes work
- [ ] Create `ColdBrewingStandMenu.java` (container)
- [ ] Create `ColdBrewingStandScreen.java` (client GUI)
- [ ] Register block, block entity, menu type

### 5.2 Integration
- [ ] Ensure all vanilla brewing recipes work
- [ ] Fuel slot accepts snowballs only
- [ ] Each snowball provides same fuel as blaze powder
- [ ] Add crafting recipe: Shimmersteel Ingots + Blue Ice

**Verification**: Craft stand, brew potions using snowballs as fuel.

---

## Phase 6: Holiday Features

**Goal**: Implement festive blocks and farming content.

### 6.1 Hearthwood Log Block
- [ ] Create `HearthwoodLogBlock.java`:
  - Has LIT blockstate property
  - Emits light when lit (level 15)
  - Lit duration: 7 in-game days (168,000 ticks)
- [ ] Create `HearthwoodLogBlockEntity.java`:
  - Track remaining burn time
  - Tick down burn time
  - Store lit time in NBT
- [ ] Create `HearthwoodLogEventHandler.java`:
  - **Frostbite immunity**: Players near lit Hearthwood Log immune to powdered snow freeze
  - **Villager discounts**: Nearby villagers offer temporary trade discounts
  - **Aurora Catalyst**: Detect Aurora Shard thrown into flame (entity collision), force aurora start

### 6.2 Shimmering Ice Block
- [ ] Create `ShimmeringIceBlock.java`:
  - Extends IceBlock behavior but never melts
  - Emits light (level 7)
  - Hydrates farmland like water
- [ ] Crafting: Water bucket + Aurora Shard → Shimmering Ice
- [ ] Or: Right-click water with Aurora Shard to transform

### 6.3 Frost-Glaze Bloom
- [ ] Create `FrostGlazeBloomBlock.java`:
  - Decorative flower block
  - Spawns naturally on snow during aurora (world gen feature or random tick)
  - Drops Frozen Petals on break
- [ ] Create world feature for aurora-time spawning (or use random tick)

### 6.4 Glow-Leeks (Crop)
- [ ] Create `GlowLeekCropBlock.java`:
  - Extends CropBlock
  - Only grows on Shimmering Ice (custom `mayPlaceOn()`)
  - Multiple growth stages
- [ ] Create `GlowLeekSeedsItem.java`
- [ ] Create `GlowLeekItem.java`:
  - Food item
  - On consume: apply Night Vision (3 min) + Glowing (3 min)
- [ ] Loot table: Mature crop drops seeds + glow-leeks

**Verification**: Test all special block features - Hearthwood Log effects, grow glow-leeks, verify Aurora Bloom spawning.

---

## Phase 7: Client Polish & Visual Effects

**Goal**: Add aurora sky rendering, particles, and visual polish.

### 7.1 Aurora Sky Rendering
- [ ] Create `AuroraRenderer.java`:
  - Hook into sky rendering (RenderLevelStageEvent or similar)
  - Render animated ribbon of neon green and pink
  - Vary intensity based on config
- [ ] Ensure minimum light level during aurora (modify ambient light)
- [ ] Register renderer in `AuroralClient.java`

### 7.2 Particles
- [ ] Create `AuroraParticle.java` custom particle
- [ ] Register particle type in `ModParticles.java`
- [ ] Spawn particles around Glacial Basin during aurora
- [ ] Spawn particles on successful infusion
- [ ] Spawn particles around Star-Shot on impact

### 7.3 Sound Effects
- [ ] Register custom sounds in sounds.json
- [ ] Aurora ambient (optional looping sound in cold biomes during aurora)
- [ ] Basin infusion sound
- [ ] Star-Shot firing and impact sounds
- [ ] Hearthwood Log crackling

### 7.4 Model & Texture Pass
- [ ] Finalize all block models
- [ ] Finalize all item models
- [ ] Create/finalize all textures
- [ ] Add animated textures where needed (Shimmering Ice, Liquid Aura)

**Verification**: Visual quality pass - aurora looks good, particles spawn correctly, sounds play.

---

## Phase 8: Data Generation & Recipes

**Goal**: Set up datagen for recipes, loot tables, tags, and lang.

### 8.1 Data Generators
- [ ] Create `ModRecipeProvider.java`:
  - All crafting recipes
  - Glacial Basin (Stone + Blue Ice)
  - Cold Brewing Stand (Shimmersteel + Blue Ice)
  - All tools and armor
- [ ] Create `ModBlockLootProvider.java`:
  - Frost-Glaze Bloom → Frozen Petals
  - Glow-Leek crops
  - All blocks self-drop with silk touch or normally
- [ ] Create `ModItemTagProvider.java` and `ModBlockTagProvider.java`:
  - Tool tags (pickaxes, axes, shovels, swords)
  - Armor tags
  - Mineable tags for blocks
- [ ] Create `ModLangProvider.java`:
  - All item/block names
  - Config descriptions
  - Chat messages

### 8.2 Advancement (Optional)
- [ ] Create advancements for progression:
  - "Cold Start" - Obtain Aurora Shard
  - "Celestial Smithing" - Craft Shimmersteel Ingot
  - "Winter Warrior" - Obtain full Shimmerweave set
  - "The Execute" - Perform an execute with Shimmersteel Sword

**Verification**: Run `./gradlew runData`, verify all JSON files generated correctly.

---

## Phase 9: Testing & Polish

**Goal**: Comprehensive testing and bug fixing.

### 9.1 Functional Testing
- [ ] Test Aurora event triggers correctly in all cold biomes
- [ ] Test Glacial Basin fills and infusion works
- [ ] Test all tool abilities:
  - Pickaxe Fortune III on gems
  - Axe copper oxidation
  - Shovel silk touch
  - Sword slowness + execute
  - Bow snowball consumption + Star-Shot
- [ ] Test all armor abilities:
  - Goggles enemy glowing
  - Tunic fire extinguish
  - Leggings speed effects
  - Skates frost/lava walker + fall immunity
- [ ] Test Cold Brewing Stand with all potion types
- [ ] Test Hearthwood Log all effects
- [ ] Test farming (Glow-Leeks on Shimmering Ice)
- [ ] Test aurora repair on equipment

### 9.2 Multiplayer Testing
- [ ] Verify aurora state syncs to all clients
- [ ] Verify all effects work correctly in multiplayer
- [ ] Check for any synchronization issues

### 9.3 Balance Pass
- [ ] Review durability values
- [ ] Review damage values
- [ ] Review effect durations
- [ ] Adjust config defaults if needed

### 9.4 Performance Testing
- [ ] Profile with Spark if needed
- [ ] Check for tick lag from aurora checks
- [ ] Check for render performance issues

**Verification**: Full playthrough from start to full gear set.

---

## Phase 10: Release Preparation

**Goal**: Prepare for release.

### 10.1 Documentation
- [ ] Write mod description for CurseForge/Modrinth
- [ ] Create feature screenshots
- [ ] Create gameplay video/gif (optional)
- [ ] Update README with features and usage

### 10.2 Build & Package
- [ ] Final `./gradlew build`
- [ ] Test JAR in clean Minecraft instance
- [ ] Verify mod loads without errors
- [ ] Verify all features work in packaged form

### 10.3 Version & Changelog
- [ ] Set proper version number in gradle.properties
- [ ] Write CHANGELOG.md
- [ ] Tag git release

---

## Implementation Priority Order

For most efficient development, follow this order:

1. **Phase 1** (Foundation) - Must be done first
2. **Phase 2** (Basin) - Core crafting mechanic
3. **Phase 3** (Tools) - Primary content
4. **Phase 4** (Armor) - Primary content
5. **Phase 8** (Datagen) - Can be done incrementally with 3 & 4
6. **Phase 5** (Brewing) - Secondary content
7. **Phase 6** (Holiday) - Secondary content
8. **Phase 7** (Polish) - Visual enhancement
9. **Phase 9** (Testing) - Quality assurance
10. **Phase 10** (Release) - Final steps

---

## Notes

- **Textures**: Placeholder textures can be used initially; final art can be added later
- **Sounds**: Can use vanilla sounds initially; custom sounds are optional polish
- **Curios**: Make integration optional - mod should work without it
- **Config**: All magic numbers should be configurable for modpack customization
