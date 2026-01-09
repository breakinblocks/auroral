package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.block.HearthwoodLogBlock;
import com.breakinblocks.auroral.block.HearthwoodLogBlockEntity;
import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * Event handler for Hearthwood Log special effects.
 * Handles:
 * - Frostbite immunity (prevents Frostbite from being applied)
 * - Villager discounts near lit Hearthwood Logs
 */
@EventBusSubscriber(modid = Auroral.MOD_ID)
public class HearthwoodLogEventHandler {

    /**
     * Radius to check for Hearthwood Logs for villager discounts.
     */
    private static final double VILLAGER_DISCOUNT_RADIUS = 16.0;

    /**
     * Discount multiplier for villagers near Hearthwood Logs (20% discount).
     */
    public static final float HEARTHWOOD_LOG_DISCOUNT = 0.8f;

    /**
     * Prevents Frostbite from being applied when the entity has Frostbite Immunity.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effectToApply = event.getEffectInstance();

        // Check if trying to apply Frostbite - use .is() for proper Holder comparison
        if (effectToApply.getEffect().is(ModEffects.FROSTBITE)) {
            // Check if entity has Frostbite Immunity
            if (entity.hasEffect(ModEffects.FROSTBITE_IMMUNITY)) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            }
        }
    }

    /**
     * Checks if a position is near a lit Hearthwood Log.
     */
    public static boolean isNearLitHearthwoodLog(Level level, BlockPos center, double radius) {
        int radiusInt = (int) Math.ceil(radius);

        for (BlockPos pos : BlockPos.betweenClosed(
            center.offset(-radiusInt, -radiusInt, -radiusInt),
            center.offset(radiusInt, radiusInt, radiusInt))) {

            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.HEARTHWOOD_LOG.get()) && state.getValue(HearthwoodLogBlock.LIT)) {
                // Check distance
                double distSq = center.distSqr(pos);
                if (distSq <= radius * radius) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the number of lit Hearthwood Logs near a position.
     */
    public static int countNearbyLitHearthwoodLogs(Level level, BlockPos center, double radius) {
        int count = 0;
        int radiusInt = (int) Math.ceil(radius);

        for (BlockPos pos : BlockPos.betweenClosed(
            center.offset(-radiusInt, -radiusInt, -radiusInt),
            center.offset(radiusInt, radiusInt, radiusInt))) {

            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.HEARTHWOOD_LOG.get()) && state.getValue(HearthwoodLogBlock.LIT)) {
                double distSq = center.distSqr(pos);
                if (distSq <= radius * radius) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Checks if a villager is near a lit Hearthwood Log for trade discounts.
     * This can be called when computing trade prices.
     */
    public static boolean villagerHasHearthwoodLogDiscount(Villager villager) {
        return isNearLitHearthwoodLog(villager.level(), villager.blockPosition(), VILLAGER_DISCOUNT_RADIUS);
    }
}
