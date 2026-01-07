package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * Shimmersteel Sword with two special abilities:
 *
 * <ul>
 *   <li><b>Chilling Strike:</b> Applies Frostbite for 3 seconds on hit</li>
 *   <li><b>Execute:</b> Instantly kills targets below 15% HP and places a snow layer at their position</li>
 * </ul>
 *
 * The execute mechanic is handled via event in ShimmersteelEventHandler.
 */
public class ShimmersteelSwordItem extends Item {

    /**
     * HP threshold for execute (15% of max HP).
     */
    public static final float EXECUTE_THRESHOLD = 0.15f;

    /**
     * Duration of frostbite effect in ticks (3 seconds = 60 ticks).
     */
    public static final int FROSTBITE_DURATION = 60;

    public ShimmersteelSwordItem(Properties properties) {
        super(properties.sword(ModToolTiers.SHIMMERSTEEL, 3, -2.4f));
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Apply frostbite on hit (thematic for Shimmersteel)
        target.addEffect(new MobEffectInstance(ModEffects.FROSTBITE, FROSTBITE_DURATION, 0));

        super.postHurtEnemy(stack, target, attacker);
    }

    /**
     * Checks if a target should be executed (HP below threshold).
     */
    public static boolean shouldExecute(LivingEntity target) {
        float healthPercent = target.getHealth() / target.getMaxHealth();
        return healthPercent < EXECUTE_THRESHOLD && healthPercent > 0;
    }

    /**
     * Places a snow layer at the target's death position.
     * Called from the event handler when execute triggers.
     */
    public static void placeSnowOnKill(Level level, BlockPos pos) {
        if (level instanceof ServerLevel) {
            // Only place snow if the space is air and the block below is solid
            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolid()) {
                level.setBlock(pos, Blocks.SNOW.defaultBlockState(), 3);
            }
        }
    }
}
