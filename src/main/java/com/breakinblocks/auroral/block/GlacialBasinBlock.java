package com.breakinblocks.auroral.block;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.auroral.registry.ModBlockEntities;
import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The Glacial Basin is the primary workstation for Auroral.
 * It collects Liquid Aura during Aurora events and allows infusion of materials.
 */
public class GlacialBasinBlock extends BaseEntityBlock {
    public static final MapCodec<GlacialBasinBlock> CODEC = simpleCodec(GlacialBasinBlock::new);

    // Aura level: 0 = empty, 1-3 = filled
    public static final int MAX_AURA_LEVEL = 3;
    public static final IntegerProperty AURA_LEVEL = IntegerProperty.create("aura_level", 0, MAX_AURA_LEVEL);

    // Cauldron-like shape
    private static final VoxelShape INSIDE = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
    private static final VoxelShape SHAPE = Shapes.join(
        Shapes.block(),
        Shapes.or(
            box(0.0, 0.0, 4.0, 16.0, 3.0, 12.0),
            box(4.0, 0.0, 0.0, 12.0, 3.0, 16.0),
            box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0),
            INSIDE
        ),
        BooleanOp.ONLY_FIRST
    );

    public GlacialBasinBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AURA_LEVEL, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AURA_LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GlacialBasinBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.GLACIAL_BASIN.get(), GlacialBasinBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof GlacialBasinBlockEntity basin)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        int auraLevel = state.getValue(AURA_LEVEL);

        // Try infusion based on item type and aura requirements
        InfusionRecipe recipe = getInfusionRecipe(stack);
        if (recipe != null) {
            if (auraLevel >= recipe.auraCost) {
                // Perform infusion
                stack.shrink(1);

                // Give result to player
                if (!player.getInventory().add(recipe.result.copy())) {
                    player.drop(recipe.result.copy(), false);
                }

                // Consume the required aura
                int newAuraLevel = auraLevel - recipe.auraCost;
                level.setBlock(pos, state.setValue(AURA_LEVEL, newAuraLevel), 3);
                basin.setAuraLevel(newAuraLevel);

                // Effects
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.0F);

                return InteractionResult.SUCCESS;
            } else {
                // Not enough aura - show message
                player.displayClientMessage(
                    Component.translatable("block.auroral.glacial_basin.not_enough_aura", recipe.auraCost),
                    true
                );
                return InteractionResult.CONSUME;
            }
        }

        // Empty hand - show status
        if (stack.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("block.auroral.glacial_basin.aura_level", auraLevel, 3),
                true
            );
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * Record for basin infusion recipes with aura cost.
     */
    public record InfusionRecipe(ItemStack result, int auraCost) {}

    /**
     * Gets the infusion recipe for the given item, including aura cost.
     * Returns null if no valid infusion exists.
     */
    public static InfusionRecipe getInfusionRecipe(ItemStack input) {
        if (input.isEmpty()) {
            return null;
        }

        // Unrefined Shimmersteel -> Shimmersteel Ingot (costs FULL aura - 3 levels)
        if (input.is(ModItems.UNREFINED_SHIMMERSTEEL.get())) {
            return new InfusionRecipe(new ItemStack(ModItems.SHIMMERSTEEL_INGOT.get()), MAX_AURA_LEVEL);
        }

        // Woven Leather -> Shimmerweave Fabric (costs 1 aura level)
        if (input.is(ModItems.WOVEN_LEATHER.get())) {
            return new InfusionRecipe(new ItemStack(ModItems.SHIMMERWEAVE_FABRIC.get()), 1);
        }

        // Frozen Petals -> Aurora Shard (costs 1 aura level)
        if (input.is(ModItems.FROZEN_PETALS.get())) {
            return new InfusionRecipe(new ItemStack(ModItems.AURORA_SHARD.get()), 1);
        }

        // Ice -> Shimmering Ice (costs 1 aura level)
        if (input.is(net.minecraft.world.item.Items.ICE)) {
            return new InfusionRecipe(new ItemStack(ModBlocks.SHIMMERING_ICE.asItem()), 1);
        }

        return null;
    }

    /**
     * Gets the result of infusing the given item.
     * Returns empty stack if no valid infusion exists.
     */
    public static ItemStack getInfusionResult(ItemStack input) {
        InfusionRecipe recipe = getInfusionRecipe(input);
        return recipe != null ? recipe.result.copy() : ItemStack.EMPTY;
    }

    /**
     * Checks if the given item can be infused in the basin.
     */
    public static boolean canInfuse(ItemStack input) {
        return getInfusionRecipe(input) != null;
    }
}
