package com.breakinblocks.auroral.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SnoreItem extends Item {

    public static final FoodProperties SNORE_FOOD = new FoodProperties.Builder()
        .nutrition(8)
        .saturationModifier(0.8f)
        .build();

    private static final int CONDUIT_POWER_DURATION = 600;

    public SnoreItem(Properties properties) {
        super(properties.food(SNORE_FOOD).stacksTo(16));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide()) {
            entity.addEffect(new MobEffectInstance(
                MobEffects.CONDUIT_POWER,
                CONDUIT_POWER_DURATION,
                0,
                false,
                true,
                true
            ));

            if (entity instanceof ServerPlayer player) {
                player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
