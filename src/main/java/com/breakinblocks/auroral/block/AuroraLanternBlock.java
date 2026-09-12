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
import net.minecraft.world.level.redstone.Orientation;

public class AuroraLanternBlock extends Block {

    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;

    private static final VoxelShape STANDING_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 9.0, 11.0);
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
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis() == Direction.Axis.Y) {
                BlockState state = this.defaultBlockState().setValue(HANGING, direction == Direction.UP);
                if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
                    return state;
                }
            }
        }
        return null;
    }

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
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            return canHangFrom(aboveState, level, above);
        } else {
            BlockPos below = pos.below();
            return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   Orientation orientation, boolean movedByPiston) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.3f) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double y = pos.getY() + (state.getValue(HANGING) ? 0.6 : 0.5);
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;

            double vy = 0.02 + random.nextDouble() * 0.02;

            level.addParticle(ModParticles.SHIMMER.get(), x, y, z, 0, vy, 0);
        }

        if (random.nextFloat() < 0.1f) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3;
            double y = pos.getY() + (state.getValue(HANGING) ? 0.7 : 0.6);
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3;

            level.addParticle(ModParticles.AURORA_SPARKLE.get(), x, y, z, 0, 0.03, 0);
        }
    }
}
