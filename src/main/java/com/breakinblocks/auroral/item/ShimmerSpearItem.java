package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.entity.ThrownShimmerSpear;
import com.breakinblocks.auroral.util.AuroraHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Shimmer Spear - A throwable spear made from Shimmersteel.
 * Similar to Trident but with aurora-themed abilities:
 * - Applies Frostbite on hit
 * - Empowered during Aurora events (more damage, longer range)
 * - Can perform a "Aurora Dash" in cold biomes (similar to Riptide)
 */
public class ShimmerSpearItem extends Item implements ProjectileItem {

    public static final int THROW_THRESHOLD_TIME = 10;
    public static final float BASE_DAMAGE = 7.0F;
    public static final float AURORA_BONUS_DAMAGE = 3.0F;
    public static final float PROJECTILE_SHOOT_POWER = 2.5F;
    public static final float AURORA_SHOOT_POWER = 3.5F;

    public ShimmerSpearItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
            .add(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_ID, BASE_DAMAGE, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            )
            .add(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.6F, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            )
            .build();
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(), 1.0F, 2, false);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        int chargeTime = this.getUseDuration(stack, entity) - timeLeft;
        if (chargeTime < THROW_THRESHOLD_TIME) {
            return false;
        }

        if (stack.nextDamageWillBreak()) {
            return false;
        }

        boolean auroraActive = AuroraHelper.isAuroraActive(level);
        boolean inColdBiome = level instanceof ServerLevel serverLevel &&
            com.breakinblocks.auroral.util.BiomeHelper.isColdBiome(serverLevel, player.blockPosition());

        player.awardStat(Stats.ITEM_USED.get(this));

        if (level instanceof ServerLevel serverLevel) {
            stack.hurtWithoutBreaking(1, player);

            // Check if Aurora Dash should trigger (in cold biome during aurora)
            if (auroraActive && inColdBiome) {
                // Aurora Dash - propel the player forward
                float yaw = player.getYRot();
                float pitch = player.getXRot();
                float dashStrength = 1.8F;

                float vx = -Mth.sin(yaw * ((float) Math.PI / 180F)) * Mth.cos(pitch * ((float) Math.PI / 180F));
                float vy = -Mth.sin(pitch * ((float) Math.PI / 180F));
                float vz = Mth.cos(yaw * ((float) Math.PI / 180F)) * Mth.cos(pitch * ((float) Math.PI / 180F));
                float magnitude = Mth.sqrt(vx * vx + vy * vy + vz * vz);
                vx = vx / magnitude * dashStrength;
                vy = vy / magnitude * dashStrength;
                vz = vz / magnitude * dashStrength;

                player.push(vx, vy, vz);
                player.startAutoSpinAttack(20, 8.0F, stack);

                if (player.onGround()) {
                    player.move(MoverType.SELF, new Vec3(0.0, 1.2, 0.0));
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
                return true;
            } else {
                // Regular throw
                ItemStack projectileStack = stack.consumeAndReturn(1, player);
                float shootPower = auroraActive ? AURORA_SHOOT_POWER : PROJECTILE_SHOOT_POWER;

                ThrownShimmerSpear thrownSpear = Projectile.spawnProjectileFromRotation(
                    ThrownShimmerSpear::new, serverLevel, projectileStack, player, 0.0F, shootPower, 1.0F
                );
                thrownSpear.setAuroraEmpowered(auroraActive);

                if (player.hasInfiniteMaterials()) {
                    thrownSpear.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }

                level.playSound(null, thrownSpear.getX(), thrownSpear.getY(), thrownSpear.getZ(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, auroraActive ? 1.3F : 1.0F);
                return true;
            }
        }

        return false;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.nextDamageWillBreak()) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
        ThrownShimmerSpear thrownSpear = new ThrownShimmerSpear(level, position.x(), position.y(), position.z(), stack.copyWithCount(1));
        thrownSpear.pickup = AbstractArrow.Pickup.ALLOWED;
        return thrownSpear;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.neoforged.neoforge.common.ItemAbility itemAbility) {
        return net.neoforged.neoforge.common.ItemAbilities.DEFAULT_TRIDENT_ACTIONS.contains(itemAbility);
    }
}
