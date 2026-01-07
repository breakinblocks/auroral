package com.breakinblocks.auroral.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shimmering Ice - A magical ice block created by infusing water with Aurora Shards.
 * Properties:
 * - Glows with inner light (light level 8)
 * - Never melts naturally
 * - Hydrates farmland like water
 * - Can be used to grow Glow-Leeks
 */
public class ShimmeringIceBlock extends HalfTransparentBlock {

    public static final MapCodec<ShimmeringIceBlock> CODEC = simpleCodec(ShimmeringIceBlock::new);

    public ShimmeringIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HalfTransparentBlock> codec() {
        return CODEC;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Shimmering Ice never melts - override ice behavior
        // No call to super.randomTick() to prevent melting
    }

    /**
     * Shimmering Ice hydrates farmland like water does.
     * This is checked by farmland blocks looking for water sources.
     */
    public static boolean canHydrate(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
    }

    /**
     * Check if a position is next to Shimmering Ice or Glowing Shimmer Ice (for farmland hydration).
     */
    public static boolean isNearShimmeringIce(LevelReader level, BlockPos farmlandPos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(
            farmlandPos.offset(-4, 0, -4),
            farmlandPos.offset(4, 1, 4))) {
            Block block = level.getBlockState(checkPos).getBlock();
            if (block instanceof ShimmeringIceBlock) {
                return true;
            }
        }
        return false;
    }
}
