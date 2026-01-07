package com.breakinblocks.auroral.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Candied Glow-Leek - A sweetened version of the Glow-Leek.
 * When eaten, grants:
 * - Night Vision (3 minutes) - much longer than raw Glow-Leek
 *
 * Unlike the raw Glow-Leek, this does NOT apply the Glowing debuff,
 * making it a pure upgrade for stealth-focused players.
 */
public class CandiedGlowLeekItem extends Item {

    public static final FoodProperties CANDIED_GLOW_LEEK_FOOD = new FoodProperties.Builder()
        .nutrition(6)
        .saturationModifier(0.8f)
        .alwaysEdible()
        .build();

    private static final int NIGHT_VISION_DURATION = 3600; // 3 minutes (vs 60 seconds for raw)

    public CandiedGlowLeekItem(Properties properties) {
        super(properties.food(CANDIED_GLOW_LEEK_FOOD));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide()) {
            // Apply extended Night Vision without the Glowing side effect
            entity.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                NIGHT_VISION_DURATION,
                0,
                false,
                true,
                true
            ));
        }

        return super.finishUsingItem(stack, level, entity);
    }
}
