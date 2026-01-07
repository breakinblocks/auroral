package com.breakinblocks.auroral.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Glow-Leek - A bioluminescent vegetable harvested from Shimmering Ice.
 * When eaten, grants:
 * - Night Vision (60 seconds)
 * - Glowing (30 seconds) - makes you visible but also helps allies find you
 */
public class GlowLeekItem extends Item {

    public static final FoodProperties GLOW_LEEK_FOOD = new FoodProperties.Builder()
        .nutrition(4)
        .saturationModifier(0.6f)
        .alwaysEdible()
        .build();

    private static final int NIGHT_VISION_DURATION = 1200; // 60 seconds
    private static final int GLOWING_DURATION = 600; // 30 seconds

    public GlowLeekItem(Properties properties) {
        super(properties.food(GLOW_LEEK_FOOD));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide()) {
            // Apply Night Vision
            entity.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                NIGHT_VISION_DURATION,
                0,
                false,
                true,
                true
            ));

            // Apply Glowing (the trade-off for night vision)
            entity.addEffect(new MobEffectInstance(
                MobEffects.GLOWING,
                GLOWING_DURATION,
                0,
                false,
                true,
                true
            ));
        }

        return super.finishUsingItem(stack, level, entity);
    }
}
