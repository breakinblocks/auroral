package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.util.SnowBlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Shimmersteel Hoe with inherent Silk Touch and snow-to-shimmer-soil tilling.
 *
 * Special abilities:
 * - Inherent Silk Touch when harvesting crops/blocks
 * - Can till snow blocks into Shimmer Soil (right-click)
 * - Standard hoe tilling behavior on dirt/grass
 *
 * The silk touch behavior is implemented via event handler in
 * {@link com.breakinblocks.auroral.events.ShimmersteelEventHandler}.
 */
public class ShimmersteelHoeItem extends Item {

    public ShimmersteelHoeItem(Properties properties) {
        super(properties.hoe(ModToolTiers.SHIMMERSTEEL, -2.0f, -1.0f));
    }

    /**
     * Checks if this item should apply silk touch effect.
     * Always returns true for Shimmersteel Hoe.
     */
    public boolean hasInherentSilkTouch() {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        // Check if clicking on snow layers (not snow block)
        if (SnowBlockHelper.isSnowLayer(state)) {
            // For snow layers, till the block underneath and remove the snow
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);

            // Check if the block below can be tilled (dirt, grass, snow block, powder snow)
            if (SnowBlockHelper.canTillToShimmerSoil(belowState)) {
                BlockState shimmerSoil = ModBlocks.SHIMMER_SOIL.get().defaultBlockState();
                if (!level.isClientSide()) {
                    // Remove snow layers
                    level.removeBlock(pos, false);
                    // Convert block below to shimmer soil
                    level.setBlock(belowPos, shimmerSoil, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, belowPos, GameEvent.Context.of(player, shimmerSoil));
                }
                level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                damageItem(context, player);
                return InteractionResult.SUCCESS;
            }
        }
        // Check if clicking on snow block or powder snow (convert directly)
        else if (SnowBlockHelper.isSolidSnow(state)) {
            // Need air above for shimmer soil
            if (level.getBlockState(pos.above()).isAir()) {
                BlockState shimmerSoil = ModBlocks.SHIMMER_SOIL.get().defaultBlockState();
                if (!level.isClientSide()) {
                    level.setBlock(pos, shimmerSoil, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, shimmerSoil));
                }
                level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                damageItem(context, player);
                return InteractionResult.SUCCESS;
            }
        }
        // Check if clicking on regular farmland (upgrade to shimmer soil)
        else if (state.is(Blocks.FARMLAND)) {
            BlockState shimmerSoil = ModBlocks.SHIMMER_SOIL.get().defaultBlockState();
            // Preserve moisture level
            int moisture = state.getValue(net.minecraft.world.level.block.FarmBlock.MOISTURE);
            shimmerSoil = shimmerSoil.setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, moisture);

            if (!level.isClientSide()) {
                level.setBlock(pos, shimmerSoil, 11);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, shimmerSoil));
            }
            level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            damageItem(context, player);
            return InteractionResult.SUCCESS;
        }
        // Check if clicking on dirt, grass, or similar blocks (till directly to shimmer soil)
        else if (SnowBlockHelper.canTillToShimmerSoil(state)) {
            // Need air above for shimmer soil (like regular farmland)
            if (level.getBlockState(pos.above()).isAir()) {
                BlockState shimmerSoil = ModBlocks.SHIMMER_SOIL.get().defaultBlockState();
                if (!level.isClientSide()) {
                    level.setBlock(pos, shimmerSoil, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, shimmerSoil));
                }
                level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                damageItem(context, player);
                return InteractionResult.SUCCESS;
            }
        }

        // No valid tilling action
        return InteractionResult.PASS;
    }

    /**
     * Helper method to damage the item after use.
     */
    private static void damageItem(UseOnContext context, Player player) {
        if (player != null) {
            context.getItemInHand().hurtAndBreak(1, player,
                context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                    ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                    : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        }
    }
}
