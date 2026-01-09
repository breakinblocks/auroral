package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.entity.StarShotEntity;
import com.breakinblocks.auroral.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Items;
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
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // Same as vanilla bow
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);

        // Check if player has snowball ammo (or is creative)
        boolean hasAmmo = player.getAbilities().instabuild || hasSnowballAmmo(player);

        if (!hasAmmo) {
            return InteractionResultHolder.fail(bowStack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(bowStack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int useDuration = this.getUseDuration(stack, entity) - timeLeft;
        if (useDuration < 3) {
            return; // Not drawn enough
        }

        // Check for ammo
        boolean isCreative = player.getAbilities().instabuild;
        ItemStack ammoStack = findSnowballAmmo(player);

        if (ammoStack.isEmpty() && !isCreative) {
            return;
        }

        if (!level.isClientSide()) {
            // Calculate power based on draw time
            float power = getPowerForTime(useDuration);

            // Create and shoot Star-Shot
            StarShotEntity starShot = new StarShotEntity(level, player);
            starShot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, power * 3.0f, 1.0f);

            // Apply item damage based on which hand is used
            EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, player, slot);

            level.addFreshEntity(starShot);

            // Play custom star shot fire sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.STAR_SHOT_FIRE.get(), SoundSource.PLAYERS, 1.0f, 1.0f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + power * 0.5f);

            // Consume ammo (unless creative)
            if (!isCreative) {
                ammoStack.shrink(1);
                if (ammoStack.isEmpty()) {
                    player.getInventory().removeItem(ammoStack);
                }
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    /**
     * Calculates power based on draw duration.
     */
    private static float getPowerForTime(int useTime) {
        float power = (float) useTime / MAX_DRAW_DURATION;
        power = (power * power + power * 2.0f) / 3.0f;
        if (power > 1.0f) {
            power = 1.0f;
        }
        return power;
    }

    /**
     * Checks if the player has snowball ammunition.
     */
    private static boolean hasSnowballAmmo(Player player) {
        return !findSnowballAmmo(player).isEmpty();
    }

    /**
     * Finds a snowball stack in the player's inventory.
     */
    private static ItemStack findSnowballAmmo(Player player) {
        // Check offhand first
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(Items.SNOWBALL)) {
            return offhand;
        }

        // Check main hand
        ItemStack mainhand = player.getMainHandItem();
        if (mainhand.is(Items.SNOWBALL)) {
            return mainhand;
        }

        // Search inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.SNOWBALL)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}
