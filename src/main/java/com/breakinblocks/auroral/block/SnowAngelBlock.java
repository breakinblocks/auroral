package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.registry.ModBlockEntities;
import com.breakinblocks.auroral.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Snow Angel - A decorative block created by sneak + right-clicking on snow.
 *
 * The snow angel imprint shows a player-shaped impression in the snow,
 * adding a cozy, playful element to snowy areas.
 *
 * Snow angels fade away after 5 minutes unless preserved with a Frozen Petal.
 */
public class SnowAngelBlock extends BaseEntityBlock {

    public static final MapCodec<SnowAngelBlock> CODEC = simpleCodec(SnowAngelBlock::new);

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty PERMANENT = BooleanProperty.create("permanent");

    /**
     * Very thin shape since it's an imprint in the snow.
     */
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);

    public SnowAngelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(PERMANENT, false));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PERMANENT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(PERMANENT, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // No collision - it's just an imprint
        return Shapes.empty();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        // Can only exist on snow blocks, powder snow, or other solid snow-like blocks
        return belowState.is(Blocks.SNOW_BLOCK) ||
               belowState.is(Blocks.POWDER_SNOW) ||
               belowState.is(Blocks.SNOW) ||
               belowState.isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   Orientation orientation, boolean movedByPiston) {
        // Check if the supporting block was removed
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, false); // No drops for snow angel imprints
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SnowAngelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, ModBlockEntities.SNOW_ANGEL.get(), SnowAngelBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Check if player is using Frozen Petals
        if (stack.is(ModItems.FROZEN_PETALS.get())) {
            // Check if already permanent
            if (state.getValue(PERMANENT)) {
                return InteractionResult.PASS;
            }

            // Make permanent
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(PERMANENT, true), 3);

                // Update the block entity
                if (level.getBlockEntity(pos) instanceof SnowAngelBlockEntity snowAngel) {
                    snowAngel.makePermanent();
                }

                // Consume one petal
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                // Effects
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.2f);
                level.addParticle(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5,
                    0, 0.05, 0);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
