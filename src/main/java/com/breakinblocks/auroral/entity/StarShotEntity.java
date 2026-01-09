package com.breakinblocks.auroral.entity;

import com.breakinblocks.auroral.registry.ModEntities;
import com.breakinblocks.auroral.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Star-Shot projectile entity.
 * Fired from Shimmersteel Bow using Snowballs as ammo.
 * Deals damage and creates a flashbang effect (Blindness + Glowing) on impact.
 */
public class StarShotEntity extends Projectile {

    private static final float DAMAGE = 6.0f;
    private static final int BLINDNESS_DURATION = 60; // 3 seconds
    private static final int GLOWING_DURATION = 100; // 5 seconds
    private static final double FLASHBANG_RADIUS = 8.0;

    private int life = 0;
    private static final int MAX_LIFE = 600; // 30 seconds max

    public StarShotEntity(EntityType<? extends StarShotEntity> entityType, Level level) {
        super(entityType, level);
    }

    public StarShotEntity(Level level, LivingEntity shooter) {
        super(ModEntities.STAR_SHOT.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
    }


    @Override
    public void tick() {
        super.tick();

        this.life++;
        if (this.life > MAX_LIFE) {
            this.discard();
            return;
        }

        // Check for collision
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.hitTargetOrDeflectSelf(hitResult);
        }

        // Update position based on velocity
        Vec3 movement = this.getDeltaMovement();
        this.move(MoverType.SELF, movement);

        // Apply gravity
        if (!this.isNoGravity()) {
            this.setDeltaMovement(movement.x, movement.y - 0.03, movement.z);
        }

        // Apply air resistance
        this.setDeltaMovement(this.getDeltaMovement().scale(0.99));

        // Spawn particles
        if (this.level().isClientSide()) {
            spawnTrailParticles();
        }
    }

    private void spawnTrailParticles() {
        // Sparkle trail effect
        this.level().addParticle(
            ParticleTypes.END_ROD,
            this.getX(), this.getY(), this.getZ(),
            0, 0, 0
        );
        this.level().addParticle(
            ParticleTypes.SNOWFLAKE,
            this.getX() + (random.nextDouble() - 0.5) * 0.2,
            this.getY() + (random.nextDouble() - 0.5) * 0.2,
            this.getZ() + (random.nextDouble() - 0.5) * 0.2,
            0, 0, 0
        );
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        Entity target = result.getEntity();
        Entity owner = this.getOwner();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            // Deal damage
            DamageSource damageSource = this.damageSources().thrown(this, owner);
            target.hurt(damageSource, DAMAGE);

            // Create flashbang effect
            createFlashbangEffect(serverLevel);
        }

        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            // Create flashbang effect on block impact
            createFlashbangEffect(serverLevel);
        }

        this.discard();
    }

    /**
     * Creates the flashbang effect - applies Blindness and Glowing to nearby entities.
     */
    private void createFlashbangEffect(ServerLevel level) {
        // Play custom impact sound
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
            ModSounds.STAR_SHOT_IMPACT.get(), SoundSource.PLAYERS, 1.5f, 1.2f);

        // Spawn explosion particles
        for (int i = 0; i < 20; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2.0;
            double offsetY = (random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0;
            level.sendParticles(ParticleTypes.END_ROD,
                this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                1, 0, 0, 0, 0.05);
        }
        for (int i = 0; i < 10; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 1.5;
            double offsetY = (random.nextDouble() - 0.5) * 1.5;
            double offsetZ = (random.nextDouble() - 0.5) * 1.5;
            level.sendParticles(ParticleTypes.END_ROD,
                this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                1, 0.0, 0.0, 0.0, 0.1);
        }

        // Apply effects to nearby entities
        Entity owner = this.getOwner();
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
            LivingEntity.class,
            this.getBoundingBox().inflate(FLASHBANG_RADIUS),
            entity -> entity != owner && entity.isAlive()
        );

        for (LivingEntity entity : nearbyEntities) {
            // Apply Blindness
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLINDNESS_DURATION, 0));
            // Apply Glowing (makes them visible through walls)
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOWING_DURATION, 0));
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        // No additional synched data needed
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(d)) {
            d = 4.0;
        }
        d *= 64.0;
        return distance < d * d;
    }
}
