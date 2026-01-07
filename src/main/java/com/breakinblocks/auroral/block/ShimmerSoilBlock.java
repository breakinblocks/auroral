package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.util.AuroraHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shimmer Soil - Aurora-infused farmland that accelerates crop growth.
 *
 * Growth bonuses:
 * - 3x faster growth at night
 * - 5x faster growth during an aurora event
 *
 * Created by tilling snow with a Shimmersteel Hoe.
 */
public class ShimmerSoilBlock extends FarmBlock {

    public ShimmerSoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Call parent for moisture handling
        super.randomTick(state, level, pos, random);

        // Check for crop above
        BlockPos cropPos = pos.above();
        BlockState cropState = level.getBlockState(cropPos);

        if (cropState.getBlock() instanceof CropBlock || cropState.getBlock() instanceof BonemealableBlock) {
            // Determine growth multiplier
            int bonusTicks = getGrowthBonusTicks(level, pos);

            // Apply bonus growth ticks
            for (int i = 0; i < bonusTicks; i++) {
                if (cropState.getBlock() instanceof CropBlock cropBlock) {
                    // Trigger the crop's random tick for growth
                    cropState.randomTick(level, cropPos, random);
                    // Re-fetch state in case it changed
                    cropState = level.getBlockState(cropPos);
                    if (!(cropState.getBlock() instanceof CropBlock)) {
                        break; // Crop was harvested or changed
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
        // During aurora: 5x growth (4 bonus ticks)
        if (AuroraHelper.isExperiencingAurora(level, pos)) {
            return 4;
        }

        // At night: 3x growth (2 bonus ticks)
        if (isNightTime(level)) {
            return 2;
        }

        // During day: normal growth (no bonus)
        return 0;
    }

    /**
     * Check if it's nighttime (between 13000 and 23000 day ticks).
     */
    private boolean isNightTime(Level level) {
        long dayTime = level.getDayTime() % 24000;
        return dayTime >= 13000 && dayTime < 23000;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Shimmer soil can survive in more conditions than regular farmland
        // It doesn't dry out as easily due to its magical nature
        return true;
    }

    /**
     * Shimmer soil emits a faint glow.
     */
    @Override
    public int getLightEmission(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return 2;
    }
}
