package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.config.AuroralConfig;
import com.breakinblocks.auroral.entity.AuroralNautilusEntity;
import com.breakinblocks.auroral.net.AuroralNetworking;
import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.registry.ModDataAttachments.AuroraState;
import com.breakinblocks.auroral.registry.ModEntities;
import com.breakinblocks.auroral.registry.ModSounds;
import com.breakinblocks.auroral.util.AuroraHelper;
import com.breakinblocks.auroral.util.BiomeHelper;
import com.breakinblocks.auroral.util.SnowBlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles aurora event triggering and lifecycle.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID)
public class AuroraEventHandler {

    // Track last day time to detect night transition (per dimension)
    private static final Map<ResourceKey<Level>, Long> lastDayTimeByDimension = new ConcurrentHashMap<>();

    // Track spawned bloom positions per dimension for O(1) removal instead of O(n³)
    private static final Map<ResourceKey<Level>, Set<BlockPos>> spawnedBloomsByDimension = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        // Only process in overworld
        if (!BiomeHelper.dimensionSupportsAurora(level)) {
            return;
        }

        ResourceKey<Level> dimensionKey = level.dimension();
        long currentDayTime = level.getDayTime() % 24000;
        long gameTime = level.getGameTime();

        // Check for night start transition (13000) - per dimension
        long lastDayTime = lastDayTimeByDimension.getOrDefault(dimensionKey, -1L);
        if (lastDayTime >= 0 && lastDayTime < 13000 && currentDayTime >= 13000) {
            onNightStart(level, gameTime);
        }

        lastDayTimeByDimension.put(dimensionKey, currentDayTime);

        // Check if current aurora should end
        AuroraState state = AuroraHelper.getAuroraState(level);
        if (state.active()) {
            // End aurora at dawn (23000) or if duration expired
            if (currentDayTime >= 23000 || currentDayTime < 13000 || state.isExpired(gameTime)) {
                endAurora(level);
            } else {
                // Aurora is active - spawn Frost-Glaze Blooms
                trySpawnFrostGlazeBlooms(level, gameTime);
                // Aurora is active - try to spawn Auroral Nautilus
                trySpawnAuroralNautilus(level, gameTime);
            }
        }
    }

    /**
     * Interval between Auroral Nautilus spawn attempts (every 10 seconds).
     */
    private static final int NAUTILUS_SPAWN_INTERVAL = 200;

    /**
     * Chance to spawn an Auroral Nautilus near each player per interval.
     */
    private static final double NAUTILUS_SPAWN_CHANCE = 0.15;

    /**
     * Maximum Auroral Nautiluses that can exist per player.
     */
    private static final int MAX_NAUTILUSES_PER_PLAYER = 3;

    /**
     * Attempts to spawn Auroral Nautilus entities near players in cold biomes during aurora.
     */
    private static void trySpawnAuroralNautilus(ServerLevel level, long gameTime) {
        // Only check every few seconds for performance
        if (gameTime % NAUTILUS_SPAWN_INTERVAL != 0) {
            return;
        }

        RandomSource random = level.getRandom();

        // Count existing nautiluses (just check all entities in the level)
        long existingNautiluses = level.getEntities(
            ModEntities.AURORAL_NAUTILUS.get(),
            entity -> true
        ).size();

        int maxTotal = level.players().size() * MAX_NAUTILUSES_PER_PLAYER;
        if (existingNautiluses >= maxTotal) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            // Only spawn near players in cold biomes
            if (!BiomeHelper.isColdBiome(level, player.blockPosition())) {
                continue;
            }

            // Random chance to spawn
            if (random.nextDouble() > NAUTILUS_SPAWN_CHANCE) {
                continue;
            }

            // Try to find a valid spawn position near the player (in the sky)
            BlockPos playerPos = player.blockPosition();
            int attempts = 5;

            for (int i = 0; i < attempts; i++) {
                int offsetX = random.nextInt(32) - 16;
                int offsetZ = random.nextInt(32) - 16;
                int height = playerPos.getY() + 10 + random.nextInt(20); // 10-30 blocks above player

                BlockPos spawnPos = new BlockPos(playerPos.getX() + offsetX, height, playerPos.getZ() + offsetZ);

                // Check if spawn position is valid (in air)
                if (level.isEmptyBlock(spawnPos) && level.isEmptyBlock(spawnPos.above())) {
                    spawnAuroralNautilus(level, spawnPos, random);
                    break;
                }
            }
        }
    }

    /**
     * Spawns an Auroral Nautilus at the given position.
     */
    private static void spawnAuroralNautilus(ServerLevel level, BlockPos pos, RandomSource random) {
        AuroralNautilusEntity nautilus = ModEntities.AURORAL_NAUTILUS.get().create(
            level,
            null,
            pos,
            EntitySpawnReason.EVENT,
            false,
            false
        );

        if (nautilus != null) {
            nautilus.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            nautilus.setYRot(random.nextFloat() * 360.0F);
            nautilus.setXRot(0.0F);
            nautilus.setNautilusSize(random.nextInt(3));

            level.addFreshEntity(nautilus);

            // Spawn appearance particles
            level.sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                15, 0.5, 0.5, 0.5, 0.1);

            // Play custom nautilus spawn sound
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                ModSounds.NAUTILUS_SPAWN.get(), SoundSource.AMBIENT, 0.8F, 1.0F);

            Auroral.LOGGER.debug("Spawned Auroral Nautilus at ({}, {}, {})",
                pos.getX(), pos.getY(), pos.getZ());
        }
    }

    /**
     * Spawning interval for Frost-Glaze Blooms (every 5 seconds).
     */
    private static final int BLOOM_SPAWN_INTERVAL = 100;

    /**
     * Chance to spawn a bloom near each player per interval.
     */
    private static final double BLOOM_SPAWN_CHANCE = 0.3;

    /**
     * Attempts to spawn Frost-Glaze Blooms near players in cold biomes during Aurora.
     */
    private static void trySpawnFrostGlazeBlooms(ServerLevel level, long gameTime) {
        // Only attempt spawn every few seconds
        if (gameTime % BLOOM_SPAWN_INTERVAL != 0) {
            return;
        }

        RandomSource random = level.getRandom();

        for (ServerPlayer player : level.players()) {
            // Only spawn near players in cold biomes
            if (!BiomeHelper.isColdBiome(level, player.blockPosition())) {
                continue;
            }

            // Random chance to spawn
            if (random.nextDouble() > BLOOM_SPAWN_CHANCE) {
                continue;
            }

            // Try to find a valid spawn position near the player
            BlockPos playerPos = player.blockPosition();
            int attempts = 5;

            for (int i = 0; i < attempts; i++) {
                int offsetX = random.nextInt(32) - 16;
                int offsetZ = random.nextInt(32) - 16;
                BlockPos checkPos = playerPos.offset(offsetX, 0, offsetZ);

                // Find surface
                BlockPos surfacePos = findSurfaceSnow(level, checkPos);
                if (surfacePos != null && canPlaceBloom(level, surfacePos)) {
                    BlockState surfaceState = level.getBlockState(surfacePos);

                    // If it's a snow layer, replace it with the bloom
                    if (SnowBlockHelper.isSnowLayer(surfaceState)) {
                        level.setBlock(surfacePos, ModBlocks.AURORA_BLOOM.get().defaultBlockState(), 3);
                        trackBloomPosition(level, surfacePos);
                        break;
                    } else {
                        // For snow blocks, powder snow, or shimmering ice, place above
                        BlockPos bloomPos = surfacePos.above();
                        if (level.isEmptyBlock(bloomPos)) {
                            level.setBlock(bloomPos, ModBlocks.AURORA_BLOOM.get().defaultBlockState(), 3);
                            trackBloomPosition(level, bloomPos);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds a snow surface block at or near the given position.
     */
    private static BlockPos findSurfaceSnow(ServerLevel level, BlockPos pos) {
        // Search from player Y up and down
        for (int y = -10; y <= 10; y++) {
            BlockPos checkPos = pos.offset(0, y, 0);
            BlockState state = level.getBlockState(checkPos);
            if (isValidBloomSurface(state)) {
                return checkPos;
            }
        }
        return null;
    }

    /**
     * Checks if a block is a valid surface for Frost-Glaze Blooms.
     */
    private static boolean isValidBloomSurface(BlockState state) {
        return SnowBlockHelper.isBloomSurface(state);
    }

    /**
     * Checks if a bloom can be placed at the given surface position.
     */
    private static boolean canPlaceBloom(ServerLevel level, BlockPos surfacePos) {
        BlockState state = level.getBlockState(surfacePos);
        return isValidBloomSurface(state);
    }

    /**
     * Bonus aurora chance per lit Hearthwood Log near players (Aurora Catalyst effect).
     */
    private static final double HEARTHWOOD_LOG_AURORA_BONUS = 0.15;

    /**
     * Called when night starts. Rolls for aurora chance.
     */
    private static void onNightStart(ServerLevel level, long gameTime) {
        // Check if there are any players in cold biomes
        boolean anyPlayerInCold = false;
        int totalHearthwoodLogs = 0;

        for (ServerPlayer player : level.players()) {
            if (BiomeHelper.isColdBiome(level, player.blockPosition())) {
                anyPlayerInCold = true;
                // Count Hearthwood Logs near this player for Aurora Catalyst effect
                totalHearthwoodLogs += HearthwoodLogEventHandler.countNearbyLitHearthwoodLogs(
                    level, player.blockPosition(), 32.0);
            }
        }

        // Only roll for aurora if at least one player is in a cold biome
        // This prevents aurora from triggering when no one can experience it
        if (!anyPlayerInCold) {
            return;
        }

        // Roll for aurora with Hearthwood Log bonus
        RandomSource random = level.getRandom();
        double baseChance = AuroralConfig.SERVER.auroraChance.get();

        // Each Hearthwood Log adds bonus chance (capped at +45% for 3 logs)
        double hearthwoodLogBonus = Math.min(totalHearthwoodLogs * HEARTHWOOD_LOG_AURORA_BONUS, 0.45);
        double totalChance = Math.min(baseChance + hearthwoodLogBonus, 0.95); // Cap at 95%

        if (totalHearthwoodLogs > 0) {
            Auroral.LOGGER.debug("Aurora Catalyst: {} Hearthwood Logs detected, chance boosted from {}% to {}%",
                totalHearthwoodLogs, (int)(baseChance * 100), (int)(totalChance * 100));
        }

        if (random.nextDouble() < totalChance) {
            startAurora(level, gameTime, random);
        }
    }

    /**
     * Starts an aurora event.
     */
    private static void startAurora(ServerLevel level, long gameTime, RandomSource random) {
        int minDuration = AuroralConfig.SERVER.auroraMinDuration.get();
        int maxDuration = AuroralConfig.SERVER.auroraMaxDuration.get();

        // Random duration between min and max
        int duration = minDuration + random.nextInt(maxDuration - minDuration + 1);

        // Clamp duration to not extend past dawn
        long currentDayTime = level.getDayTime() % 24000;
        long ticksUntilDawn = (23000 - currentDayTime + 24000) % 24000;
        duration = (int) Math.min(duration, ticksUntilDawn);

        AuroraHelper.startAurora(level, duration);

        Auroral.LOGGER.info("Aurora started! Duration: {} ticks ({} seconds)",
            duration, duration / 20);

        // Play aurora start sound for all players in cold biomes
        for (ServerPlayer player : level.players()) {
            if (BiomeHelper.isColdBiome(level, player.blockPosition())) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.AURORA_START.get(), SoundSource.AMBIENT, 1.0f, 1.0f);
            }
        }

        // Sync to all clients
        AuroralNetworking.syncAuroraToAll(level, true);
    }

    /**
     * Ends the current aurora event.
     */
    private static void endAurora(ServerLevel level) {
        AuroraState currentState = AuroraHelper.getAuroraState(level);
        if (!currentState.active()) {
            return;
        }

        AuroraHelper.endAurora(level);

        Auroral.LOGGER.info("Aurora ended.");

        // Remove aurora blooms near all players
        removeAuroraBlooms(level);

        // Play aurora end sound for all players in cold biomes
        for (ServerPlayer player : level.players()) {
            if (BiomeHelper.isColdBiome(level, player.blockPosition())) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.AURORA_END.get(), SoundSource.AMBIENT, 1.0f, 1.0f);
            }
        }

        // Sync to all clients
        AuroralNetworking.syncAuroraToAll(level, false);
    }

    /**
     * Tracks a bloom position for efficient removal later.
     * O(1) insertion instead of requiring O(n³) search on removal.
     */
    private static void trackBloomPosition(ServerLevel level, BlockPos pos) {
        ResourceKey<Level> dimensionKey = level.dimension();
        spawnedBloomsByDimension
            .computeIfAbsent(dimensionKey, k -> ConcurrentHashMap.newKeySet())
            .add(pos.immutable());
    }

    /**
     * Removes a bloom position from tracking (e.g., when player harvests it).
     */
    public static void untrackBloomPosition(ServerLevel level, BlockPos pos) {
        ResourceKey<Level> dimensionKey = level.dimension();
        Set<BlockPos> blooms = spawnedBloomsByDimension.get(dimensionKey);
        if (blooms != null) {
            blooms.remove(pos);
        }
    }

    /**
     * Removes all tracked aurora blooms when the aurora ends.
     * Uses position tracking for O(n) removal instead of O(n³) cube search.
     */
    private static void removeAuroraBlooms(ServerLevel level) {
        ResourceKey<Level> dimensionKey = level.dimension();
        Set<BlockPos> blooms = spawnedBloomsByDimension.remove(dimensionKey);

        if (blooms == null || blooms.isEmpty()) {
            return;
        }

        int totalRemoved = 0;

        for (BlockPos pos : blooms) {
            // Skip if chunk isn't loaded
            if (!level.isLoaded(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.AURORA_BLOOM.get())) {
                // Remove the bloom with particles
                level.destroyBlock(pos, false);
                level.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    5, 0.3, 0.3, 0.3, 0.05);
                totalRemoved++;
            }
        }

        if (totalRemoved > 0) {
            Auroral.LOGGER.debug("Removed {} aurora blooms at sunrise", totalRemoved);
        }
    }

    /**
     * Forces an aurora to start (e.g., from Hearthwood Log + Aurora Shard).
     * Can be called from other parts of the mod.
     */
    public static void forceStartAurora(ServerLevel level) {
        if (!BiomeHelper.dimensionSupportsAurora(level)) {
            return;
        }

        // Only start if it's night and no aurora is active
        if (!AuroraHelper.isNightTime(level)) {
            return;
        }

        AuroraState currentState = AuroraHelper.getAuroraState(level);
        if (currentState.active()) {
            return;
        }

        startAurora(level, level.getGameTime(), level.getRandom());
    }
}
