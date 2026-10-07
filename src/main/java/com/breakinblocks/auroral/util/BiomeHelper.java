package com.breakinblocks.auroral.util;

import com.breakinblocks.auroral.config.AuroralConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for biome-related checks.
 */
public class BiomeHelper {

    /**
     * Checks if the biome at the given position is considered "cold" for aurora purposes.
     * Cold biomes include snowy biomes and any biome where it snows.
     *
     * @param level The level to check
     * @param pos The position to check
     * @return true if the biome is cold
     */
    public static boolean isColdBiome(Level level, BlockPos pos) {
        Holder<Biome> biomeHolder = level.getBiome(pos);
        Biome biome = biomeHolder.value();

        // Check if tagged as snowy (IS_SNOWY tag marks snowy biomes)
        if (biomeHolder.is(BiomeTags.IS_TAIGA) || biomeHolder.is(BiomeTags.IS_MOUNTAIN)) {
            return true;
        }

        // Check if the biome has cold enough temperature for snow
        if (biome.coldEnoughToSnow(pos)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the given level/dimension supports aurora events.
     *
     * @param level The level to check
     * @return true if the dimension can have aurora
     */
    public static boolean dimensionSupportsAurora(Level level) {
        return auroraDimensions().contains(level.dimension().location());
    }

    private static Set<ResourceLocation> auroraDimensions() {
        List<? extends String> configured = AuroralConfig.SERVER.auroraDimensions.get();
        DimensionCache cache = dimensionCache;
        if (cache.source() != configured) {
            cache = new DimensionCache(configured, configured.stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet()));
            dimensionCache = cache;
        }
        return cache.dimensions();
    }

    private record DimensionCache(List<? extends String> source, Set<ResourceLocation> dimensions) {}

    private static volatile DimensionCache dimensionCache = new DimensionCache(List.of(), Set.of());

    /**
     * Checks if a player at the given position can see/benefit from aurora.
     * The player must be in a cold biome and in a dimension that supports aurora.
     *
     * @param level The level
     * @param pos The position to check
     * @return true if aurora effects apply at this position
     */
    public static boolean canExperienceAurora(Level level, BlockPos pos) {
        return dimensionSupportsAurora(level) && isColdBiome(level, pos);
    }
}
