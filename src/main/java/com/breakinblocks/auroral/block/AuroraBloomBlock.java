package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.util.AuroraHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
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

/**
 * Aurora Bloom - A crystalline flower that spawns on snow during Aurora events.
 *
 * Has 4 growth stages (age 0-3). Only the final stage (age=3) drops
 * Frozen Petals (1-4 depending on fortune). Harvesting early yields nothing.
 *
 * Properties:
 * - Spawns naturally during Aurora on snow blocks
 * - Grows slowly over time during aurora events
 * - Emits soft light and sparkle particles (intensity increases with age)
 */
public class AuroraBloomBlock extends BushBlock implements BonemealableBlock {

    public static final MapCodec<AuroraBloomBlock> CODEC = simpleCodec(AuroraBloomBlock::new);

    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    // Shapes for each growth stage
    private static final VoxelShape[] SHAPES = new VoxelShape[] {
        Block.box(5.0, 0.0, 5.0, 11.0, 4.0, 11.0),   // Stage 0 - tiny sprout
        Block.box(4.0, 0.0, 4.0, 12.0, 7.0, 12.0),   // Stage 1 - small
        Block.box(3.0, 0.0, 3.0, 13.0, 10.0, 13.0),  // Stage 2 - medium
        Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0)   // Stage 3 - full bloom
    };

    public AuroraBloomBlock(Properties properties) {
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
        // Can be placed on snow, snow blocks, powder snow, Shimmering Ice,
        // or any solid block (for when bloom replaces snow layers)
        return state.is(Blocks.SNOW) ||
               state.is(Blocks.SNOW_BLOCK) ||
               state.is(Blocks.POWDER_SNOW) ||
               state.getBlock() instanceof ShimmeringIceBlock ||
               state.isSolid();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }

    /**
     * Check if the bloom is fully grown.
     */
    public boolean isMaxAge(BlockState state) {
        return state.getValue(AGE) >= MAX_AGE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Check if we should die (if not on valid block)
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, false); // Don't drop anything if dying
            return;
        }

        // Aurora Blooms wilt and disappear during daytime
        if (!AuroraHelper.isNightTime(level)) {
            // Spawn wilt particles before destroying
            level.sendParticles(ParticleTypes.SNOWFLAKE,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                5, 0.3, 0.3, 0.3, 0.01);
            level.destroyBlock(pos, false); // Don't drop anything when wilting
            return;
        }

        // Grow if not at max age (slow growth, ~5% chance per random tick)
        if (!isMaxAge(state) && random.nextFloat() < 0.05f) {
            int newAge = state.getValue(AGE) + 1;
            level.setBlock(pos, state.setValue(AGE, newAge), 2);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);

        // Sparkle particles (more frequent at higher ages)
        if (random.nextInt(6 - age) == 0) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double y = pos.getY() + 0.1 + (age * 0.15) + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;

            level.addParticle(ParticleTypes.END_ROD, x, y, z,
                (random.nextDouble() - 0.5) * 0.02,
                random.nextDouble() * 0.02,
                (random.nextDouble() - 0.5) * 0.02);
        }

        // Occasional snowflake (only at higher ages)
        if (age >= 2 && random.nextInt(10) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.5 + (age * 0.2);
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0, -0.02, 0);
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        // Light increases with age: 3, 5, 6, 8
        return 3 + (state.getValue(AGE) * 2) - (state.getValue(AGE) > 2 ? 1 : 0);
    }

    // BonemealableBlock implementation

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        // Can only apply bonemeal if not fully grown
        return !isMaxAge(state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        // 75% chance of success
        return random.nextFloat() < 0.75f;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        // Advance to next growth stage
        int newAge = Math.min(MAX_AGE, state.getValue(AGE) + 1);
        level.setBlock(pos, state.setValue(AGE, newAge), 2);
    }
}
