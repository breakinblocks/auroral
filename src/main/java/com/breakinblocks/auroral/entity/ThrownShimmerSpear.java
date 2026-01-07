package com.breakinblocks.auroral.entity;

import com.breakinblocks.auroral.registry.ModEffects;
import com.breakinblocks.auroral.registry.ModEntities;
import com.breakinblocks.auroral.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Thrown Shimmer Spear projectile entity.
 * Applies Frostbite on hit and deals bonus damage during Aurora.
 */
public class ThrownShimmerSpear extends AbstractArrow {

    private static final EntityDataAccessor<Boolean> ID_AURORA_EMPOWERED =
        SynchedEntityData.defineId(ThrownShimmerSpear.class, EntityDataSerializers.BOOLEAN);

    private static final float BASE_DAMAGE = 8.0F;
    private static final float AURORA_BONUS_DAMAGE = 4.0F;
    private static final int FROSTBITE_DURATION = 100; // 5 seconds
    private static final int FROSTBITE_AMPLIFIER = 0;
    private static final int AURORA_FROSTBITE_AMPLIFIER = 1;

    private boolean dealtDamage;
    private ItemStack spearItem = new ItemStack(ModItems.SHIMMER_SPEAR.get());

    public ThrownShimmerSpear(EntityType<? extends ThrownShimmerSpear> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownShimmerSpear(Level level, LivingEntity shooter, ItemStack spearStack) {
        super(ModEntities.THROWN_SHIMMER_SPEAR.get(), shooter, level, spearStack, null);
        this.spearItem = spearStack.copy();
    }

    public ThrownShimmerSpear(Level level, double x, double y, double z, ItemStack spearStack) {
        super(ModEntities.THROWN_SHIMMER_SPEAR.get(), x, y, z, level, spearStack, null);
        this.spearItem = spearStack.copy();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_AURORA_EMPOWERED, false);
    }

    public void setAuroraEmpowered(boolean empowered) {
        this.entityData.set(ID_AURORA_EMPOWERED, empowered);
    }

    public boolean isAuroraEmpowered() {
        return this.entityData.get(ID_AURORA_EMPOWERED);
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        super.tick();

        // Spawn particles while flying
        if (this.level().isClientSide() && !this.isInGround()) {
            spawnTrailParticles();
        }
    }

    private void spawnTrailParticles() {
        Vec3 vel = this.getDeltaMovement();
        double speed = vel.length();

        if (speed > 0.1) {
            // Snowflake trail
            this.level().addParticle(
                ParticleTypes.SNOWFLAKE,
                this.getX(), this.getY(), this.getZ(),
                vel.x * -0.1, vel.y * -0.1, vel.z * -0.1
            );

            // End rod particles if aurora empowered
            if (isAuroraEmpowered() && this.random.nextFloat() < 0.3f) {
                this.level().addParticle(
                    ParticleTypes.END_ROD,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    0, 0.02, 0
                );
            }
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return this.spearItem.copy();
    }

    @Override
    protected @Nullable EntityHitResult findHitEntity(Vec3 startVec, Vec3 endVec) {
        return this.dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner = this.getOwner();

        float damage = BASE_DAMAGE;
        boolean auroraEmpowered = isAuroraEmpowered();

        if (auroraEmpowered) {
            damage += AURORA_BONUS_DAMAGE;
        }

        DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);
        this.dealtDamage = true;

        if (target.hurtOrSimulate(damageSource, damage)) {
            if (target.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (target instanceof LivingEntity livingTarget) {
                // Apply Frostbite effect
                int amplifier = auroraEmpowered ? AURORA_FROSTBITE_AMPLIFIER : FROSTBITE_AMPLIFIER;
                int duration = auroraEmpowered ? FROSTBITE_DURATION * 2 : FROSTBITE_DURATION;
                livingTarget.addEffect(new MobEffectInstance(ModEffects.FROSTBITE.getDelegate(), duration, amplifier));

                // Knockback
                this.doKnockback(livingTarget, damageSource);
            }
        }

        // Spawn impact particles
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,
                this.getX(), this.getY(), this.getZ(),
                10, 0.3, 0.3, 0.3, 0.1
            );
            if (auroraEmpowered) {
                serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    this.getX(), this.getY(), this.getZ(),
                    5, 0.2, 0.2, 0.2, 0.05
                );
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));

        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Override
    protected boolean tryPickup(Player player) {
        return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player player) {
        if (this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}
