package com.breakinblocks.auroral.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.FarmlandWaterManager;
import net.neoforged.neoforge.common.ticket.AABBTicket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shimmering Ice - A magical ice block created by infusing water with Aurora Shards.
 * Properties:
 * - Glows with inner light (light level 8)
 * - Never melts naturally
 * - Hydrates vanilla farmland and Shimmer Soil within 4 blocks via NeoForge's FarmlandWaterManager
 * - Can be used to grow Glow-Leeks
 */
public class ShimmeringIceBlock extends HalfTransparentBlock {

    public static final MapCodec<ShimmeringIceBlock> CODEC = simpleCodec(ShimmeringIceBlock::new);

    // Tickets keyed by ice position so each placement's hydration area can be cleanly invalidated on removal.
    private static final Map<ResourceKey<Level>, Map<BlockPos, AABBTicket>> TICKETS = new ConcurrentHashMap<>();

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

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel && !state.is(oldState.getBlock())) {
            registerWaterTicket(serverLevel, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel && !state.is(newState.getBlock())) {
            removeWaterTicket(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
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

    /**
     * Registers an AABB water ticket around the given ice position so vanilla farmland's
     * {@code FarmlandWaterManager.hasBlockWaterTicket} lookup returns true. No-op if one
     * is already registered for this position.
     */
    public static void registerWaterTicket(ServerLevel level, BlockPos pos) {
        BlockPos immut = pos.immutable();
        Map<BlockPos, AABBTicket> perLevel =
            TICKETS.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
        AABBTicket existing = perLevel.get(immut);
        if (existing != null && existing.isValid()) {
            return;
        }
        // Matches the 4-block horizontal / 1-block vertical radius used by vanilla farmland hydration checks.
        AABB aabb = new AABB(
            immut.getX() - 4, immut.getY(), immut.getZ() - 4,
            immut.getX() + 5, immut.getY() + 2, immut.getZ() + 5);
        AABBTicket ticket = FarmlandWaterManager.addAABBTicket(level, aabb);
        perLevel.put(immut, ticket);
    }

    public static void removeWaterTicket(ServerLevel level, BlockPos pos) {
        Map<BlockPos, AABBTicket> perLevel = TICKETS.get(level.dimension());
        if (perLevel == null) {
            return;
        }
        AABBTicket ticket = perLevel.remove(pos.immutable());
        if (ticket != null) {
            ticket.invalidate();
        }
    }
}
