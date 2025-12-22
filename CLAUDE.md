# Aurorabound - Project Guidelines

> "The sky gives, the ice remembers."

## Project Overview

Aurorabound is a comprehensive winter expansion mod for Minecraft 1.21.1 (NeoForge) centered around celestial energy, mystical metallurgy, and specialized survival. The mod transforms cold biomes from barren wastes into hubs of high-tier progression by harnessing the Aurora event.

## Core Systems

### 1. Aurora Event System (The Celestial Engine)
- **Trigger**: 33% chance each night in Cold biomes
- **Effects**: Sky ribbons (green/pink), prevents total darkness
- **Mechanical Impact**: Enables Liquid Aura collection, repairs Shimmersteel gear
- **Data Storage**: Use Level Data Attachment to track aurora state per dimension

### 2. Glacial Basin (Primary Workstation)
- **Type**: Block with BlockEntity (no GUI - right-click interaction)
- **Recipe**: Stone + Blue Ice
- **Mechanics**:
  - Fills with Liquid Aura during Aurora
  - Right-click with items to infuse (Iron→Shimmersteel, Leather/Wool→Shimmerweave)
- **Implementation**: Use `BlockEntity` with tick method, `use()` for item transformation

### 3. Shimmersteel Armory
- **Tier**: Slightly better than Iron, high enchantability
- **Special Abilities**:
  - Pickaxe: Fortune III for gems only
  - Axe: Forces copper oxidation on right-click
  - Shovel: Silk Touch inherent
  - Sword: Slowness I on hit, Execute at <15% HP (place snow layer on kill)
  - Bow: Uses Snowballs as ammo → Star-Shots (damage + flashbang)

### 4. Shimmerweave Armor
- **Goggles**: Head/Curio slot, Glowing to hostiles in 32-block radius
- **Tunic**: Auto-extinguish fire
- **Leggings**: Speed on snow, Soul Speed on soul sand
- **Skates**: Speed I on ice/snow, no friction, Frost Walker, Lava→Obsidian, fall immunity on ice/obsidian

### 5. Cold Brewing Stand
- **Purpose**: Nether-free alchemy path
- **Fuel**: Snowballs instead of Blaze Powder
- **Recipe**: Shimmersteel + Blue Ice

### 6. Holiday Features
- **Yule Log**: Burns 7 days, frostbite immunity, villager discounts, Aurora Catalyst
- **Shimmering Ice**: Water + Aurora Shard, glows, never freezes, hydrates
- **Glow-Leeks**: Grown on Shimmering Ice, grants Night Vision + Glowing
- **Frost-Glaze Blooms**: Spawn on snow during Aurora, drop Frozen Petals

## Package Structure

```
dev/saereth/aurorabound/
├── Aurorabound.java                 # Main mod class
├── api/                             # Public API (if needed for addons)
├── client/
│   ├── AuroraboundClient.java       # Client initialization
│   ├── renderer/
│   │   ├── AuroraRenderer.java      # Sky aurora effect
│   │   ├── GlacialBasinRenderer.java
│   │   └── StarShotRenderer.java
│   └── particle/
│       └── AuroraParticle.java
├── config/
│   └── AuroraboundConfig.java       # Server/Client configs
├── data/
│   └── AuroraLevelData.java         # Level attachment for aurora state
├── events/
│   ├── AuroraEventHandler.java      # Aurora tick/trigger logic
│   ├── ShimmersteelEventHandler.java # Tool/armor special abilities
│   └── YuleLogEventHandler.java     # Holiday features
├── block/
│   ├── GlacialBasinBlock.java
│   ├── GlacialBasinBlockEntity.java
│   ├── ColdBrewingStandBlock.java
│   ├── YuleLogBlock.java
│   ├── ShimmeringIceBlock.java
│   └── FrostGlazeBloomBlock.java
├── item/
│   ├── ShimmersteelPickaxeItem.java
│   ├── ShimmersteelAxeItem.java
│   ├── ShimmersteelShovelItem.java
│   ├── ShimmersteelSwordItem.java
│   ├── ShimmersteelBowItem.java
│   ├── ShimmersteelGogglesItem.java
│   ├── ShimmerweaveTunicItem.java
│   ├── ShimmerweaveLeggingsItem.java
│   ├── ShimmerweaveSkatesItem.java
│   └── GlowLeekItem.java
├── entity/
│   └── StarShotEntity.java          # Projectile for Shimmersteel Bow
├── net/
│   ├── AuroraboundNetworking.java
│   └── SyncAuroraPayload.java       # Sync aurora state to clients
├── registry/
│   ├── ModBlocks.java
│   ├── ModItems.java
│   ├── ModBlockEntities.java
│   ├── ModEntities.java
│   ├── ModCreativeTabs.java
│   ├── ModDataAttachments.java
│   └── ModParticles.java
├── integration/
│   └── CuriosIntegration.java       # Curios API for goggles
└── util/
    ├── AuroraHelper.java            # Aurora detection utilities
    └── BiomeHelper.java             # Cold biome detection
```

## Key Technical Patterns

### Aurora State Management
```java
// Level Data Attachment for aurora state
public record AuroraState(boolean active, long startTick, long endTick) {
    public static final AuroraState INACTIVE = new AuroraState(false, 0, 0);
}

// Check in event handler each night transition
@SubscribeEvent
public static void onLevelTick(LevelTickEvent.Post event) {
    if (event.getLevel() instanceof ServerLevel level) {
        // Check for night start, roll aurora chance
    }
}
```

### Execute Mechanic (Sword)
```java
@SubscribeEvent
public static void onLivingHurt(LivingHurtEvent event) {
    // Check if attacker holds Shimmersteel Sword
    // If target HP < 15% max, set damage to massive value
    // On kill, place snow layer at entity position
}
```

### Lava-to-Obsidian (Skates)
```java
@SubscribeEvent
public static void onPlayerTick(PlayerTickEvent.Post event) {
    // Check for Shimmerweave Skates equipped
    // Extend FrostWalker logic: check block below
    // If lava, replace with obsidian
}
```

### Basin Infusion (No GUI)
```java
@Override
public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    ItemStack held = player.getMainHandItem();
    if (held.is(Items.IRON_INGOT) && hasLiquidAura()) {
        held.shrink(1);
        player.addItem(new ItemStack(ModItems.SHIMMERSTEEL_INGOT.get()));
        consumeAura();
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
    // ... other transformations
}
```

## Configuration Values

```java
// Server Config
auroraChance = 0.33          // 33% per night
auroraRepairRate = 1         // Durability per second
executeThreshold = 0.15      // 15% HP for execute
yuleLogBurnTime = 168000     // 7 days in ticks
glowingRadius = 32           // Goggles effect radius

// Client Config
showAuroraParticles = true
auroraIntensity = 1.0
```

## Dependencies

### Required
- NeoForge 21.1.55+
- Minecraft 1.21.1

### Optional Integration
- Curios API (for goggles slot)

## Asset Requirements

### Textures Needed
- Blocks: glacial_basin, cold_brewing_stand, yule_log, shimmering_ice, frost_glaze_bloom
- Items: All Shimmersteel tools, all Shimmerweave armor, shimmersteel_ingot, shimmerweave_fabric, aurora_shard, frozen_petals, glow_leek, glow_leek_seeds
- Entity: star_shot projectile
- Particles: aurora_sparkle

### Models
- Block models (standard cube variants)
- Item models (handheld tools, armor)
- Star-Shot entity model

### Sounds
- Aurora ambient
- Basin infusion
- Star-Shot impact
- Yule Log crackling

---

# NeoForge 1.21.1 Development Reference

The sections below provide general NeoForge 1.21.1 patterns and best practices.

## Registration Pattern (NeoForge 1.21.1 Style)

```java
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Aurorabound.MOD_ID);

    public static final DeferredBlock<Block> GLACIAL_BASIN = BLOCKS.register("glacial_basin",
        () -> new GlacialBasinBlock(BlockBehaviour.Properties.of()
            .strength(2.0f)
            .requiresCorrectToolForDrops())
    );
}

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Aurorabound.MOD_ID);

    public static final DeferredItem<Item> SHIMMERSTEEL_INGOT = ITEMS.register("shimmersteel_ingot",
        () -> new Item(new Item.Properties())
    );
}
```

## Main Mod Class Pattern

```java
@Mod(Aurorabound.MOD_ID)
public class Aurorabound {
    public static final String MOD_ID = "aurorabound";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public Aurorabound(IEventBus eventBus, ModContainer container, Dist dist) {
        ModBlocks.BLOCKS.register(eventBus);
        ModItems.ITEMS.register(eventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(eventBus);
        ModEntities.ENTITIES.register(eventBus);
        ModDataAttachments.ATTACHMENT_TYPES.register(eventBus);

        container.registerConfig(ModConfig.Type.SERVER, AuroraboundConfig.serverSpec);

        if (dist.isClient()) {
            container.registerConfig(ModConfig.Type.CLIENT, AuroraboundConfig.clientSpec);
            AuroraboundClient.init(eventBus);
        }
    }
}
```

## Event Handler Pattern

```java
@EventBusSubscriber(modid = Aurorabound.MOD_ID)
public class AuroraEventHandler {
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            // Aurora logic
        }
    }
}
```

## Data Attachments (Level/Entity Data)

```java
public class ModDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Aurorabound.MOD_ID);

    public static final Supplier<AttachmentType<AuroraState>> AURORA_STATE = ATTACHMENT_TYPES.register(
        "aurora_state",
        () -> AttachmentType.builder(() -> AuroraState.INACTIVE)
            .serialize(AuroraState.CODEC)
            .build()
    );
}
```

## Networking (Payload System)

```java
public record SyncAuroraPayload(boolean active) implements CustomPacketPayload {
    public static final Type<SyncAuroraPayload> TYPE = new Type<>(Aurorabound.id("sync_aurora"));

    public static final StreamCodec<FriendlyByteBuf, SyncAuroraPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, SyncAuroraPayload::active,
        SyncAuroraPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
```

## Best Practices

1. **Always use DeferredRegister** with specialized variants (.createBlocks(), .createItems())
2. **Data Components** for item data, **Data Attachments** for entity/level data
3. **Check `level.isClientSide()`** before server-only logic
4. **Use `@EventBusSubscriber`** annotation for automatic event registration
5. **Separate client code** into client package with Dist.CLIENT checks
6. **Use Codecs** for all serialization
7. **Create helper `id()` method** for ResourceLocation creation
