package com.breakinblocks.auroral.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnderBloomBlock extends BushBlock implements BonemealableBlock {

    public static final MapCodec<EnderBloomBlock> CODEC = simpleCodec(EnderBloomBlock::new);

    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
        Block.box(5.0, 0.0, 5.0, 11.0, 4.0, 11.0),
        Block.box(4.0, 0.0, 4.0, 12.0, 7.0, 12.0),
        Block.box(3.0, 0.0, 3.0, 13.0, 10.0, 13.0),
        Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0)
    };

    public EnderBloomBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<BushBlock> codec() {
        return (MapCodec<BushBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SNOW) ||
               state.is(Blocks.SNOW_BLOCK) ||
               state.is(Blocks.POWDER_SNOW) ||
               state.is(Blocks.END_STONE) ||
               state.getBlock() instanceof ShimmeringIceBlock ||
               state.getBlock() instanceof ShimmerSoilBlock ||
               state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }

    public boolean isMaxAge(BlockState state) {
        return state.getValue(AGE) >= MAX_AGE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, false);
            return;
        }

        if (!isMaxAge(state) && canGrowOn(level.getBlockState(pos.below()))
                && random.nextFloat() < 0.05f) {
            int newAge = state.getValue(AGE) + 1;
            level.setBlock(pos, state.setValue(AGE, newAge), 2);
        }
    }

    private static boolean canGrowOn(BlockState below) {
        return below.getBlock() instanceof ShimmerSoilBlock || below.is(Blocks.END_STONE);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);

        if (random.nextInt(6 - age) == 0) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double y = pos.getY() + 0.1 + (age * 0.15) + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;

            level.addParticle(ParticleTypes.PORTAL, x, y, z,
                (random.nextDouble() - 0.5) * 0.02,
                random.nextDouble() * 0.02,
                (random.nextDouble() - 0.5) * 0.02);
        }

        if (age >= 2 && random.nextInt(10) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.5 + (age * 0.2);
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0, -0.02, 0);
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 3 + (state.getValue(AGE) * 2) - (state.getValue(AGE) > 2 ? 1 : 0);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !isMaxAge(state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.75f;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int newAge = Math.min(MAX_AGE, state.getValue(AGE) + 1);
        level.setBlock(pos, state.setValue(AGE, newAge), 2);
    }
}
