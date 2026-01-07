package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.registry.ModBlockEntities;
import com.breakinblocks.auroral.registry.ModParticles;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Hearthwood Log - A cozy block that burns for 7 days.
 * Provides:
 * - Frostbite immunity to nearby players
 * - Villager trade discounts
 * - Acts as Aurora Catalyst (increases aurora chance)
 */
public class HearthwoodLogBlock extends BaseEntityBlock {

    public static final MapCodec<HearthwoodLogBlock> CODEC = simpleCodec(HearthwoodLogBlock::new);

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    protected static final VoxelShape SHAPE_X = Block.box(0.0, 0.0, 2.0, 16.0, 8.0, 14.0);
    protected static final VoxelShape SHAPE_Z = Block.box(2.0, 0.0, 0.0, 14.0, 8.0, 16.0);

    public HearthwoodLogBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(LIT, true)
            .setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(AXIS, context.getHorizontalDirection().getAxis())
            .setValue(LIT, true);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HearthwoodLogBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, ModBlockEntities.HEARTHWOOD_LOG.get(), HearthwoodLogBlockEntity::serverTick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, AXIS);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }

        // Fire crackling sound
        if (random.nextInt(24) == 0) {
            level.playLocalSound(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                0.5f + random.nextFloat(), random.nextFloat() * 0.7f + 0.6f, false
            );
        }

        // Fire particles
        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + 0.25 + random.nextDouble() * 0.5;
            double y = pos.getY() + 0.5 + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.25 + random.nextDouble() * 0.5;

            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0, 0.02, 0.0);
        }

        // Frost particles (instead of smoke for magical cold fire)
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.25 + random.nextDouble() * 0.5;
            double y = pos.getY() + 0.6;
            double z = pos.getZ() + 0.25 + random.nextDouble() * 0.5;
            level.addParticle(ModParticles.FROST.get(), x, y, z, 0.0, 0.05, 0.0);
        }

        // Occasional sparkle particles (cozy touch)
        if (random.nextInt(10) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.5 + random.nextDouble() * 0.5;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.END_ROD, x, y, z,
                (random.nextDouble() - 0.5) * 0.05,
                random.nextDouble() * 0.05,
                (random.nextDouble() - 0.5) * 0.05);
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }
}
