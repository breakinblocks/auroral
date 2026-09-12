package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.Auroral;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ShimmerSpearItem extends Item {

    public static final float BASE_DAMAGE = 2.0F;
    public static final float ATTACK_SPEED = -2.95F;
    public static final double EXTRA_REACH = 1.5;
    public static final double REACH = 4.5;
    public static final int CHARGE_TICKS = 15;
    public static final int MIN_CHARGE_TICKS = 5;
    public static final int STRIKE_COOLDOWN_TICKS = 20;
    public static final float FULL_CHARGE_MULTIPLIER = 2.0F;

    private static final int PIERCE_INVULNERABLE_TICKS = 10;

    public ShimmerSpearItem(Properties properties) {
        super(properties
            .durability(ModToolTiers.SHIMMERSTEEL.getUses())
            .attributes(createAttributes()));
    }

    private static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_ID,
                    BASE_DAMAGE + ModToolTiers.SHIMMERSTEEL.getAttackDamageBonus(),
                    AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_ID, ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(Auroral.id("shimmer_spear_reach"), EXTRA_REACH, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .build();
    }

    @Override
    public int getEnchantmentValue() {
        return ModToolTiers.SHIMMERSTEEL.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return ModToolTiers.SHIMMERSTEEL.getRepairIngredient().test(repairCandidate);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player) || player.getUsedItemHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        int usedTicks = this.getUseDuration(stack, entity) - timeLeft;
        if (usedTicks < MIN_CHARGE_TICKS) {
            return;
        }

        float charge = Math.min(1.0F, usedTicks / (float) CHARGE_TICKS);
        Vec3 look = player.getLookAngle();

        if (level instanceof ServerLevel serverLevel) {
            player.push(look.x * 0.4 * charge, 0.0, look.z * 0.4 * charge);
            player.hurtMarked = true;

            float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * (1.0F + (FULL_CHARGE_MULTIPLIER - 1.0F) * charge);
            List<Entity> targets = getEntitiesAlongLook(player, REACH, e -> canPierce(player, e));
            boolean hit = false;
            for (Entity target : targets) {
                hit |= stab(serverLevel, player, stack, target, damage, look, 0.6F * charge);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                hit ? SoundEvents.PLAYER_ATTACK_STRONG : SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1.0F, 0.9F + charge * 0.2F);

            EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, player, slot);
            player.getCooldowns().addCooldown(this, STRIKE_COOLDOWN_TICKS);
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        player.swing(player.getUsedItemHand(), true);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);

        if (!(attacker instanceof Player player) || !(attacker.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        Vec3 look = player.getLookAngle();
        for (Entity other : getEntitiesAlongLook(player, REACH, e -> e != target && canPierce(player, e))) {
            stab(serverLevel, player, stack, other, damage, look, 0.4F);
        }
    }

    private static boolean canPierce(Player player, Entity entity) {
        return entity != player
            && entity.isAlive()
            && entity.isPickable()
            && !entity.isSpectator()
            && !player.isPassengerOfSameVehicle(entity)
            && (!(entity instanceof Player other) || player.canHarmPlayer(other));
    }

    private static boolean stab(ServerLevel level, Player player, ItemStack stack, Entity target,
                                float damage, Vec3 look, float knockback) {
        if (target.invulnerableTime > PIERCE_INVULNERABLE_TICKS) {
            return false;
        }
        DamageSource source = player.damageSources().playerAttack(player);
        float finalDamage = EnchantmentHelper.modifyDamage(level, stack, target, source, damage);
        if (!target.hurt(source, finalDamage)) {
            return false;
        }
        if (target instanceof LivingEntity living && knockback > 0) {
            living.knockback(knockback, -look.x, -look.z);
        }
        EnchantmentHelper.doPostAttackEffectsWithItemSource(level, target, source, stack);
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
            SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 0.8F, 1.2F);
        return true;
    }

    public static List<Entity> getEntitiesAlongLook(LivingEntity attacker, double reach, java.util.function.Predicate<Entity> filter) {
        Vec3 start = attacker.getEyePosition();
        Vec3 look = attacker.getLookAngle();
        Vec3 end = start.add(look.scale(reach));

        BlockHitResult blockHit = attacker.level().clip(new ClipContext(start, end,
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, attacker));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        AABB sweep = attacker.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0);
        List<Entity> hits = new ArrayList<>();
        for (Entity entity : attacker.level().getEntities(attacker, sweep, filter)) {
            if (entity.getBoundingBox().inflate(0.3).clip(start, end).isPresent()) {
                hits.add(entity);
            }
        }
        return hits;
    }
}
