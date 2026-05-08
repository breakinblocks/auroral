package com.breakinblocks.auroral.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AuroraBloomDecorativeBlock extends BushBlock {

    public static final MapCodec<AuroraBloomDecorativeBlock> CODEC = simpleCodec(AuroraBloomDecorativeBlock::new);

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0);

    public AuroraBloomDecorativeBlock(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<BushBlock> codec() {
        return (MapCodec<BushBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SNOW) ||
               state.is(Blocks.SNOW_BLOCK) ||
               state.is(Blocks.POWDER_SNOW) ||
               state.getBlock() instanceof ShimmeringIceBlock ||
               state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 8;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double y = pos.getY() + 0.55 + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;

            level.addParticle(ParticleTypes.END_ROD, x, y, z,
                (random.nextDouble() - 0.5) * 0.02,
                random.nextDouble() * 0.02,
                (random.nextDouble() - 0.5) * 0.02);
        }

        if (random.nextInt(10) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.9;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0, -0.02, 0);
        }
    }
}
