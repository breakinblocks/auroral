package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.entity.StarShotEntity;
import com.breakinblocks.auroral.registry.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * Shimmersteel Bow - Uses Snowballs as ammunition.
 * Fires Star-Shot projectiles that deal damage and create a flashbang effect.
 */
public class ShimmersteelBowItem extends Item {

    public static final int MAX_DRAW_DURATION = 20; // Ticks to fully draw (faster than vanilla bow)

    public ShimmersteelBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // Same as vanilla bow
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);

        boolean hasInfinity = level.registryAccess()
            .lookup(Registries.ENCHANTMENT)
            .flatMap(reg -> reg.get(Enchantments.INFINITY))
            .map(holder -> bowStack.getEnchantmentLevel(holder) > 0)
            .orElse(false);

        boolean hasAmmo = player.getAbilities().instabuild || hasInfinity || hasSnowballAmmo(player);

        if (!hasAmmo) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        int useDuration = this.getUseDuration(stack, entity) - timeLeft;
        if (useDuration < 3) {
            return false; // Not drawn enough
        }

        boolean isCreative = player.getAbilities().instabuild;
        boolean hasInfinity = level.registryAccess()
            .lookup(Registries.ENCHANTMENT)
            .flatMap(reg -> reg.get(Enchantments.INFINITY))
            .map(holder -> stack.getEnchantmentLevel(holder) > 0)
            .orElse(false);
        ItemStack ammoStack = findSnowballAmmo(player);

        if (ammoStack.isEmpty() && !isCreative && !hasInfinity) {
            return false;
        }

        if (level instanceof ServerLevel serverLevel) {
            float power = getPowerForTime(useDuration);

            StarShotEntity starShot = new StarShotEntity(level, player);
            // Pass the bow stack so Power and other weapon enchantments apply on hit.
            starShot.setWeaponItem(stack);
            starShot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, power * 3.0f, 1.0f);
            EnchantmentHelper.onProjectileSpawned(serverLevel, stack, starShot, item -> {});

            EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, player, slot);

            level.addFreshEntity(starShot);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.STAR_SHOT_FIRE.get(), SoundSource.PLAYERS, 1.0f, 1.0f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + power * 0.5f);

            if (!isCreative && !hasInfinity) {
                ammoStack.shrink(1);
                if (ammoStack.isEmpty()) {
                    player.getInventory().removeItem(ammoStack);
                }
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }

    private static float getPowerForTime(int useTime) {
        float power = (float) useTime / MAX_DRAW_DURATION;
        power = (power * power + power * 2.0f) / 3.0f;
        if (power > 1.0f) {
            power = 1.0f;
        }
        return power;
    }

    private static boolean hasSnowballAmmo(Player player) {
        return !findSnowballAmmo(player).isEmpty();
    }

    private static ItemStack findSnowballAmmo(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(Items.SNOWBALL)) {
            return offhand;
        }

        ItemStack mainhand = player.getMainHandItem();
        if (mainhand.is(Items.SNOWBALL)) {
            return mainhand;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.SNOWBALL)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}
