package com.breakinblocks.auroral.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Shimmersteel Axe that forces copper oxidation on right-click.
 *
 * When right-clicking on copper blocks, this axe advances the oxidation
 * state instead of removing it (opposite of normal axe behavior).
 */
public class ShimmersteelAxeItem extends Item {

    public ShimmersteelAxeItem(Properties properties) {
        super(properties.axe(ModToolTiers.SHIMMERSTEEL, 6.0f, -3.1f));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        // Check if this is a weatherable copper block
        if (state.getBlock() instanceof WeatheringCopper weatheringBlock) {
            // Get the next oxidation state
            Optional<BlockState> nextState = weatheringBlock.getNext(state);

            if (nextState.isPresent()) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, nextState.get(), Block.UPDATE_ALL_IMMEDIATE);

                    // Damage the tool
                    if (player != null) {
                        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                    }
                }

                // Play oxidation sound
                level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);

                return InteractionResult.SUCCESS;
            }
        }

        // Fall back to normal axe behavior (stripping logs, etc.)
        return super.useOn(context);
    }
}
