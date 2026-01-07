package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Glow-Leek - A bioluminescent crop that grows on Shimmering Ice.
 * Unlike normal crops, it doesn't need farmland - it grows directly on Shimmering Ice.
 * When consumed, grants Night Vision and Glowing effects.
 */
public class GlowLeekBlock extends CropBlock {

    public static final MapCodec<GlowLeekBlock> CODEC = simpleCodec(GlowLeekBlock::new);

    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);

    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
        Block.box(4.0, 0.0, 4.0, 12.0, 4.0, 12.0),
        Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0),
        Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0),
        Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0)
    };

    public GlowLeekBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends CropBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[this.getAge(state)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // Can only be planted on Shimmering Ice
        return state.getBlock() instanceof ShimmeringIceBlock;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.GLOW_LEEK_SEEDS.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Growth doesn't require light for Glow-Leeks (they make their own)
        if (!level.isAreaLoaded(pos, 1)) {
            return;
        }

        if (this.getAge(state) < this.getMaxAge()) {
            // Slower growth rate than normal crops
            if (random.nextInt(8) == 0) {
                level.setBlock(pos, this.getStateForAge(this.getAge(state) + 1), 2);
            }
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        // Glows more as it grows
        return 3 + (getAge(state) * 2); // 3, 5, 7, 9 light levels
    }
}
