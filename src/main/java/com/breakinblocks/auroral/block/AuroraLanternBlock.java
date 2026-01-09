package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Aurora Lantern - A decorative light source that captures and emits aurora light.
 *
 * This enchanting lantern glows with the ethereal colors of the aurora,
 * providing both light and ambient shimmer particles. Can be placed
 * standing or hanging from chains/other blocks.
 */
public class AuroraLanternBlock extends Block {

    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;

    /**
     * Shape when standing on ground.
     */
    private static final VoxelShape STANDING_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 9.0, 11.0);

    /**
     * Shape when hanging from above.
     */
    private static final VoxelShape HANGING_SHAPE = Block.box(5.0, 2.0, 5.0, 11.0, 11.0, 11.0);

    public AuroraLanternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HANGING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Check if there's a block above to hang from
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction == Direction.UP) {
                BlockPos above = context.getClickedPos().above();
                BlockState aboveState = context.getLevel().getBlockState(above);
                if (canHangFrom(aboveState, context.getLevel(), above)) {
                    return this.defaultBlockState().setValue(HANGING, true);
                }
            }
        }
        return this.defaultBlockState().setValue(HANGING, false);
    }

    /**
     * Check if the lantern can hang from a given block state.
     */
    private static boolean canHangFrom(BlockState state, LevelReader level, BlockPos pos) {
        return state.isFaceSturdy(level, pos, Direction.DOWN) ||
               state.is(Blocks.IRON_BARS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? HANGING_SHAPE : STANDING_SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HANGING)) {
            // Hanging lanterns need a block above
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            return canHangFrom(aboveState, level, above);
        } else {
            // Standing lanterns need a solid surface below
            BlockPos below = pos.below();
            return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        // Check if the supporting block was removed
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Spawn shimmer particles for ambient glow effect
        if (random.nextFloat() < 0.3f) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double y = pos.getY() + (state.getValue(HANGING) ? 0.6 : 0.5);
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;

            // Small upward drift
            double vy = 0.02 + random.nextDouble() * 0.02;

            level.addParticle(ModParticles.SHIMMER.get(), x, y, z, 0, vy, 0);
        }

        // Occasional aurora sparkle
        if (random.nextFloat() < 0.1f) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3;
            double y = pos.getY() + (state.getValue(HANGING) ? 0.7 : 0.6);
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3;

            level.addParticle(ModParticles.AURORA_SPARKLE.get(), x, y, z, 0, 0.03, 0);
        }
    }
}
