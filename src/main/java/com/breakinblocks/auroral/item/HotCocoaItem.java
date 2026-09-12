package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;

/**
 * Hot Cocoa - A warm, cozy drink that provides comfort in cold biomes.
 * When consumed, grants:
 * - Regeneration I (30 seconds)
 * - Frostbite Immunity (5 minutes)
 *
 * The milk bucket is returned by the crafting recipe.
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
        super(properties.food(HOT_COCOA_FOOD, Consumables.DEFAULT_DRINK).stacksTo(16));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide()) {
            entity.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION,
                REGEN_DURATION,
                0,
                false,
                true,
                true
            ));

            entity.addEffect(new MobEffectInstance(
                ModEffects.FROSTBITE_IMMUNITY,
                FROSTBITE_IMMUNITY_DURATION,
                0,
                false,
                true,
                true
            ));
        }

        return super.finishUsingItem(stack, level, entity);
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
