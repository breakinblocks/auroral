package com.breakinblocks.auroral.util;

import com.breakinblocks.auroral.registry.ModBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Utility class for snow-related block checks.
 * Consolidates repeated snow detection logic across the codebase.
 */
public final class SnowBlockHelper {

    private SnowBlockHelper() {
        // Utility class - no instantiation
    }

    /**
     * Checks if the block is any type of vanilla snow (layer, block, or powder).
     */
    public static boolean isSnow(BlockState state) {
        return state.is(Blocks.SNOW) ||
               state.is(Blocks.SNOW_BLOCK) ||
               state.is(Blocks.POWDER_SNOW);
    }

    /**
     * Checks if the block is a solid snow surface (snow block or powder snow).
     * Does not include snow layers.
     */
    public static boolean isSolidSnow(BlockState state) {
        return state.is(Blocks.SNOW_BLOCK) ||
               state.is(Blocks.POWDER_SNOW);
    }

    /**
     * Checks if the block is a snow layer (not a full block).
     */
    public static boolean isSnowLayer(BlockState state) {
        return state.is(Blocks.SNOW);
    }

    /**
     * Checks if the block is a valid surface for Aurora Blooms.
     * Includes vanilla snow types and Shimmering Ice.
     */
    public static boolean isBloomSurface(BlockState state) {
        return isSnow(state) ||
               state.is(ModBlocks.SHIMMERING_ICE.get());
    }

    /**
     * Checks if the block is a valid base for snow-related features.
     * Includes vanilla snow types and Shimmering Ice.
     */
    public static boolean isSnowOrIce(BlockState state) {
        return isSnow(state) ||
               state.is(ModBlocks.SHIMMERING_ICE.get());
    }

    /**
     * Checks if the block can be tilled to Shimmer Soil.
     * Includes snow types and various dirt variants.
     */
    public static boolean canTillToShimmerSoil(BlockState state) {
        return state.is(Blocks.SNOW_BLOCK) ||
               state.is(Blocks.POWDER_SNOW) ||
               state.is(Blocks.DIRT) ||
               state.is(Blocks.GRASS_BLOCK) ||
               state.is(Blocks.DIRT_PATH) ||
               state.is(Blocks.COARSE_DIRT) ||
               state.is(Blocks.ROOTED_DIRT);
    }
}
