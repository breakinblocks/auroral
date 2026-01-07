package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.registry.ModParticles;
import com.breakinblocks.auroral.util.SnowBlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Event handler for creating Snow Angels.
 *
 * When a player sneaks and right-clicks on a snow block with an empty hand,
 * a snow angel imprint is created above the block.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID)
public class SnowAngelEventHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();

        // Only trigger when sneaking with empty main hand
        if (!player.isShiftKeyDown()) {
            return;
        }

        if (!player.getMainHandItem().isEmpty()) {
            return;
        }

        // Only process main hand to avoid double processing
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        // Check if clicked block is snow
        if (!isSnowBlock(clickedState)) {
            return;
        }

        // Determine where to place the snow angel
        BlockPos angelPos;

        // If clicking on a snow layer, replace it
        if (SnowBlockHelper.isSnowLayer(clickedState)) {
            angelPos = clickedPos;
        } else {
            // For snow blocks/powder snow, place above
            angelPos = clickedPos.above();
            // Check if there's space above
            if (!level.getBlockState(angelPos).isAir()) {
                return;
            }
        }

        // Create the snow angel facing opposite to the player's direction
        Direction facing = player.getDirection().getOpposite();
        BlockState angelState = ModBlocks.SNOW_ANGEL.get().defaultBlockState()
            .setValue(com.breakinblocks.auroral.block.SnowAngelBlock.FACING, facing);

        // Place the snow angel
        level.setBlockAndUpdate(angelPos, angelState);

        // Play sound and spawn particles
        level.playSound(null, angelPos, SoundEvents.SNOW_PLACE, SoundSource.BLOCKS, 1.0f, 1.2f);

        if (level instanceof ServerLevel serverLevel) {
            // Spawn shimmer particles around the snow angel
            for (int i = 0; i < 12; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
                double offsetY = level.random.nextDouble() * 0.3;
                double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;

                serverLevel.sendParticles(
                    ModParticles.SHIMMER.get(),
                    angelPos.getX() + 0.5 + offsetX,
                    angelPos.getY() + 0.1 + offsetY,
                    angelPos.getZ() + 0.5 + offsetZ,
                    1,
                    0.05, 0.05, 0.05,
                    0.01
                );
            }
        }

        // Swing player's arm and consume interaction
        player.swing(InteractionHand.MAIN_HAND, true);
        event.setCanceled(true);
    }

    /**
     * Check if a block state is a valid snow block for creating snow angels.
     */
    private static boolean isSnowBlock(BlockState state) {
        return SnowBlockHelper.isSnow(state);
    }
}
