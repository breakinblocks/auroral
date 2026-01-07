package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.registry.ModEffects;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;

/**
 * Hot Cocoa - A warm, cozy drink that provides comfort in cold biomes.
 * When consumed, grants:
 * - Regeneration I (30 seconds)
 * - Frostbite Immunity (5 minutes)
 *
 * Returns an empty bucket when consumed (since it's made with milk bucket).
 */
public class HotCocoaItem extends Item {

    public static final FoodProperties HOT_COCOA_FOOD = new FoodProperties.Builder()
        .nutrition(4)
        .saturationModifier(0.3f)
        .alwaysEdible()
        .build();

    private static final int REGEN_DURATION = 600; // 30 seconds
    private static final int FROSTBITE_IMMUNITY_DURATION = 6000; // 5 minutes

    public HotCocoaItem(Properties properties) {
        super(properties.food(HOT_COCOA_FOOD).stacksTo(16));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        }

        if (!level.isClientSide()) {
            // Apply Regeneration I
            entity.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION,
                REGEN_DURATION,
                0,
                false,
                true,
                true
            ));

            // Apply Frostbite Immunity
            entity.addEffect(new MobEffectInstance(
                ModEffects.FROSTBITE_IMMUNITY,
                FROSTBITE_IMMUNITY_DURATION,
                0,
                false,
                true,
                true
            ));
        }

        // Return empty bucket after drinking (made with milk bucket)
        if (entity instanceof Player player) {
            return ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET));
        }

        stack.shrink(1);
        return stack.isEmpty() ? new ItemStack(Items.BUCKET) : stack;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32; // Same as vanilla drinks
    }
}
