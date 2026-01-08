package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.item.ShimmersteelHoeItem;
import com.breakinblocks.auroral.item.ShimmersteelPickaxeItem;
import com.breakinblocks.auroral.item.ShimmersteelShovelItem;
import com.breakinblocks.auroral.item.ShimmersteelSwordItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Event handler for Shimmersteel tool special abilities.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID)
public class ShimmersteelEventHandler {

    /**
     * Handles the execute mechanic for Shimmersteel Sword.
     * If a target would be at or below the execute threshold after taking damage, they die instantly.
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();

        // Check if attacker is using Shimmersteel Sword
        if (source.getEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();

            if (weapon.getItem() instanceof ShimmersteelSwordItem) {
                LivingEntity target = event.getEntity();

                // Don't execute the attacker themselves
                if (target == attacker) {
                    return;
                }

                // Calculate health AFTER this damage would be applied
                float currentHealth = target.getHealth();
                float damageAmount = event.getNewDamage();
                float healthAfterDamage = currentHealth - damageAmount;
                float maxHealth = target.getMaxHealth();
                float executeThreshold = com.breakinblocks.auroral.config.AuroralConfig.SERVER.executeThreshold.get().floatValue();
                float thresholdHealth = maxHealth * executeThreshold;

                // Execute if: currently below threshold OR would drop to/below threshold from this hit
                boolean alreadyBelowThreshold = currentHealth < thresholdHealth && currentHealth > 0;
                boolean wouldDropBelowThreshold = healthAfterDamage <= thresholdHealth && currentHealth > 0;

                if (alreadyBelowThreshold || wouldDropBelowThreshold) {
                    // Set damage to a very high value to ensure death
                    event.setNewDamage(Float.MAX_VALUE);

                    // Schedule snow placement (will be placed on death)
                    if (target.level() instanceof ServerLevel serverLevel) {
                        BlockPos deathPos = target.blockPosition();
                        // Use a small delay to ensure the entity has died
                        serverLevel.getServer().execute(() -> {
                            if (target.isDeadOrDying()) {
                                ShimmersteelSwordItem.placeSnowOnKill(serverLevel, deathPos);
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Handles Fortune III for Shimmersteel Pickaxe on gem blocks
     * and Silk Touch for Shimmersteel Shovel.
     */
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        Entity breaker = event.getBreaker();
        if (breaker == null) {
            return;
        }

        ItemStack tool = event.getTool();
        BlockState state = event.getState();
        ServerLevel level = event.getLevel();
        BlockPos pos = event.getPos();

        // Shimmersteel Pickaxe: Fortune III for gems
        if (tool.getItem() instanceof ShimmersteelPickaxeItem) {
            if (ShimmersteelPickaxeItem.isGemBlock(state)) {
                applyFortuneBonus(event.getDrops(), level.getRandom(), 3);
            }
        }

        // Shimmersteel Shovel: Silk Touch
        if (tool.getItem() instanceof ShimmersteelShovelItem) {
            applySilkTouch(event, state, pos);
        }

        // Shimmersteel Hoe: Silk Touch (for crops and other blocks)
        if (tool.getItem() instanceof ShimmersteelHoeItem) {
            applySilkTouch(event, state, pos);
        }
    }

    /**
     * Applies Fortune bonus to drops by multiplying item counts.
     * Uses vanilla Fortune formula: bonus = random(0 to fortuneLevel) + 1
     */
    private static void applyFortuneBonus(List<ItemEntity> drops, RandomSource random, int fortuneLevel) {
        for (ItemEntity itemEntity : drops) {
            ItemStack stack = itemEntity.getItem();
            // Only apply fortune to stackable items (the actual drops, not blocks)
            if (stack.getMaxStackSize() > 1) {
                // Fortune formula: multiplier is 1 + random(0 to fortuneLevel)
                // So Fortune III gives 1-4x drops
                int multiplier = 1 + random.nextInt(fortuneLevel + 1);
                if (multiplier > 1) {
                    int newCount = Math.min(stack.getCount() * multiplier, stack.getMaxStackSize());
                    stack.setCount(newCount);
                }
            }
        }
    }

    /**
     * Replaces normal drops with silk touch drops (the block itself).
     */
    private static void applySilkTouch(BlockDropsEvent event, BlockState state, BlockPos pos) {
        // Get the block's silk touch drop
        Block block = state.getBlock();
        ItemStack silkDrop = new ItemStack(block);

        // Only apply if the block would give a different drop normally
        // and if it's a valid silk touch target
        if (silkDrop.isEmpty()) {
            return;
        }

        // Clear existing drops and add the block
        List<ItemEntity> drops = event.getDrops();
        drops.clear();

        // Create new item entity with the silk touched block
        ServerLevel level = event.getLevel();
        ItemEntity newDrop = new ItemEntity(
            level,
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5,
            silkDrop
        );
        newDrop.setDefaultPickUpDelay();
        drops.add(newDrop);
    }
}
