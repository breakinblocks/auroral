package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.entity.AuroralSnowletteEntity;
import com.breakinblocks.auroral.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Handles the transformation of Snow Golems into Auroral Snowlettes
 * when right-clicked with an Aurora Shard.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID)
public class SnowletteEventHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        // Check if interacting with a Snow Golem
        if (!(event.getTarget() instanceof SnowGolem snowGolem)) {
            return;
        }

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);

        // Check if holding an Aurora Shard
        if (!heldItem.is(ModItems.AURORA_SHARD.get())) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();

        // Consume the shard (unless creative)
        if (!player.getAbilities().instabuild) {
            heldItem.shrink(1);
        }

        // Play transformation sound
        level.playSound(null, snowGolem.getX(), snowGolem.getY(), snowGolem.getZ(),
            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.playSound(null, snowGolem.getX(), snowGolem.getY(), snowGolem.getZ(),
            SoundEvents.SNOW_GOLEM_AMBIENT, SoundSource.NEUTRAL, 1.0F, 1.5F);

        // Create the Snowlette at the Snow Golem's position
        AuroralSnowletteEntity.createFromSnowGolem(
            level,
            snowGolem.getX(),
            snowGolem.getY(),
            snowGolem.getZ(),
            player
        );

        // Remove the Snow Golem
        snowGolem.discard();

        // Cancel the event and swing arm
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
