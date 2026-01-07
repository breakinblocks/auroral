package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.registry.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Frosted Cookies - Delicious cookies with a shimmering frost glaze.
 * A cozy, stackable snack that spawns shimmer particles when eaten.
 *
 * These cookies are fast to eat (like vanilla cookies) and provide
 * a small but satisfying amount of nutrition.
 */
public class FrostedCookiesItem extends Item {

    public static final FoodProperties FROSTED_COOKIES_FOOD = new FoodProperties.Builder()
        .nutrition(3)
        .saturationModifier(0.4f)
        .build();

    /**
     * Time to eat in ticks - 16 ticks is fast like vanilla cookies.
     */
    private static final int EAT_DURATION = 16;

    public FrostedCookiesItem(Properties properties) {
        super(properties.food(FROSTED_COOKIES_FOOD));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return EAT_DURATION;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // Spawn shimmer particles around the entity when eating
        if (level instanceof ServerLevel serverLevel) {
            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight() * 0.5;
            double z = entity.getZ();

            // Spawn a burst of shimmer particles
            for (int i = 0; i < 8; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (level.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (level.random.nextDouble() - 0.5) * 0.5;

                serverLevel.sendParticles(
                    ModParticles.SHIMMER.get(),
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1, // count
                    0.1, 0.1, 0.1, // spread
                    0.02 // speed
                );
            }
        }

        return super.finishUsingItem(stack, level, entity);
    }
}
