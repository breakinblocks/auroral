package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.util.AuroraHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.TriState;

/**
 * Shimmer Soil - Aurora-infused farmland that accelerates crop growth.
 *
 * Growth bonuses:
 * - 3x faster growth at night
 * - 5x faster growth during an aurora event
 *
 * Created by tilling snow with a Shimmersteel Hoe.
 */
public class ShimmerSoilBlock extends FarmlandBlock {

    public ShimmerSoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int currentMoisture = state.getValue(MOISTURE);
        if (isNearWaterOrShimmeringIce(level, pos)) {
            if (currentMoisture < 7) {
                level.setBlock(pos, state.setValue(MOISTURE, 7), 2);
            }
        } else if (currentMoisture > 0) {
            level.setBlock(pos, state.setValue(MOISTURE, currentMoisture - 1), 2);
        } else if (!hasCrops(level, pos)) {
            // Shimmer soil never reverts to dirt - skip vanilla turnToDirt behavior
        }

        BlockPos cropPos = pos.above();
        BlockState cropState = level.getBlockState(cropPos);

        if (cropState.getBlock() instanceof CropBlock || cropState.getBlock() instanceof BonemealableBlock) {
            int bonusTicks = getGrowthBonusTicks(level, pos);

            for (int i = 0; i < bonusTicks; i++) {
                if (cropState.getBlock() instanceof CropBlock cropBlock) {
                    cropState.randomTick(level, cropPos, random);
                    cropState = level.getBlockState(cropPos);
                    if (!(cropState.getBlock() instanceof CropBlock)) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Calculate bonus growth ticks based on time and aurora state.
     *
     * @return Number of extra growth ticks (0 during day, 2 at night, 4 during aurora)
     */
    private int getGrowthBonusTicks(Level level, BlockPos pos) {
        if (AuroraHelper.isExperiencingAurora(level, pos)) {
            return 4;
        }

        if (isNightTime(level)) {
            return 2;
        }

        return 0;
    }

    private boolean isNightTime(Level level) {
        long dayTime = level.getOverworldClockTime() % 24000;
        return dayTime >= 13000 && dayTime < 23000;
    }

    private boolean isNearWaterOrShimmeringIce(LevelReader level, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (level.getFluidState(checkPos).is(FluidTags.WATER)) {
                return true;
            }
            if (level.getBlockState(checkPos).getBlock() instanceof ShimmeringIceBlock) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCrops(LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return above.getBlock() instanceof CropBlock || above.getBlock() instanceof BonemealableBlock;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.causeFallDamage(fallDistance, 1.0F, entity.damageSources().fall());
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction direction, BlockState plantState) {
        if (direction == Direction.UP && (plantState.getBlock() instanceof CropBlock || plantState.getBlock() instanceof BonemealableBlock)) {
            return TriState.TRUE;
        }
        return TriState.DEFAULT;
    }

    @Override
    public int getLightEmission(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return 2;
    }
}
