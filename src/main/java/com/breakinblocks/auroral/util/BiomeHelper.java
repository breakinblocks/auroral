package com.breakinblocks.auroral.util;

import com.breakinblocks.auroral.config.AuroralConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
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

    private static final TagKey<Biome> IS_COLD = TagKey.create(
        Registries.BIOME,
        Identifier.fromNamespaceAndPath("c", "is_cold")
    );

    /**
     * Checks if the biome at the given position is considered "cold" for aurora purposes.
     * Uses the common {@code c:is_cold} biome tag as the sole criterion.
     *
     * @param level The level to check
     * @param pos The position to check
     * @return true if the biome is tagged {@code c:is_cold}
     */
    public static boolean isColdBiome(Level level, BlockPos pos) {
        Holder<Biome> biomeHolder = level.getBiome(pos);
        return biomeHolder.is(IS_COLD);
    }

    /**
     * Checks if the given level/dimension supports aurora events.
     *
     * @param level The level to check
     * @return true if the dimension can have aurora
     */
    public static boolean dimensionSupportsAurora(Level level) {
        return auroraDimensions().contains(level.dimension().identifier());
    }

    private static Set<Identifier> auroraDimensions() {
        List<? extends String> configured = AuroralConfig.SERVER.auroraDimensions.get();
        DimensionCache cache = dimensionCache;
        if (cache.source() != configured) {
            cache = new DimensionCache(configured, configured.stream()
                .map(Identifier::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet()));
            dimensionCache = cache;
        }
        return cache.dimensions();
    }

    private record DimensionCache(List<? extends String> source, Set<Identifier> dimensions) {}

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
