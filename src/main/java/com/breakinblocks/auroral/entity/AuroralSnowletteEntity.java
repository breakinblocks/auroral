package com.breakinblocks.auroral.entity;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Auroral Snowlette - A tiny, friendly pet Snow Golem created by using an Aurora Shard on a Snow Golem.
 *
 * Features:
 * - 1/4 the size of a normal Snow Golem
 * - Follows its owner like a pet
 * - Can be healed by throwing snowballs at it
 * - Emits gentle aurora sparkles
 */
public class AuroralSnowletteEntity extends TamableAnimal {

    public AuroralSnowletteEntity(EntityType<? extends AuroralSnowletteEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createLivingAttributes()
            .add(Attributes.MAX_HEALTH, 8.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        // Aurora sparkle particles
        if (this.level().isClientSide()) {
            if (this.tickCount % 10 == 0 && this.random.nextFloat() < 0.7f) {
                // Occasional END_ROD particle for aurora effect
                double offsetX = (this.random.nextDouble() - 0.5) * 0.4;
                double offsetY = this.random.nextDouble() * 0.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.4;
                this.level().addParticle(
                    ParticleTypes.END_ROD,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    0, 0.02, 0
                );
            }

            // Occasional snowflake when moving
            if (this.tickCount % 5 == 0 && this.getDeltaMovement().lengthSqr() > 0.001) {
                this.level().addParticle(
                    ParticleTypes.SNOWFLAKE,
                    this.getX(),
                    this.getY() + 0.2,
                    this.getZ(),
                    0, -0.01, 0
                );
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // Check if hit by a snowball - heal instead of damage
        if (source.getDirectEntity() instanceof Snowball) {
            // Heal by 2 hearts when hit by snowball
            this.heal(4.0F);

            // Happy particles
            level.sendParticles(ParticleTypes.HEART, getX(), getY() + 0.5, getZ(), 3, 0.3, 0.3, 0.3, 0);
            level.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 0.3, getZ(), 8, 0.3, 0.3, 0.3, 0.05);

            // Play happy sound
            this.playSound(SoundEvents.SNOW_GOLEM_AMBIENT, 1.0F, 1.2F);

            return false; // Don't take damage
        }

        return super.hurtServer(level, source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SNOW_GOLEM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SNOW_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SNOW_GOLEM_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        // Quieter since it's small
        return 0.5F;
    }

    @Override
    public float getVoicePitch() {
        // Higher pitch since it's small
        return 1.4F + (this.random.nextFloat() - 0.5F) * 0.2F;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        // Snowlettes don't breed
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        // Snowlettes don't eat food for breeding
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Only owner can toggle sitting
        if (this.isTame() && this.isOwnedBy(player)) {
            if (!this.level().isClientSide()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    /**
     * Create a Snowlette from a Snow Golem's position and owner.
     */
    public static AuroralSnowletteEntity createFromSnowGolem(ServerLevel level, double x, double y, double z, Player owner) {
        AuroralSnowletteEntity snowlette = ModEntities.AURORAL_SNOWLETTE.get().create(level, EntitySpawnReason.CONVERSION);
        if (snowlette != null) {
            snowlette.setPos(x, y, z);
            snowlette.setTame(true, false);
            snowlette.setOwner(owner);
            level.addFreshEntity(snowlette);

            // Spawn transformation particles
            level.sendParticles(ParticleTypes.END_ROD, x, y + 0.5, z, 30, 0.5, 0.8, 0.5, 0.1);
            level.sendParticles(ParticleTypes.SNOWFLAKE, x, y + 0.5, z, 20, 0.5, 0.8, 0.5, 0.1);

            Auroral.LOGGER.debug("Created Auroral Snowlette at ({}, {}, {}) for player {}", x, y, z, owner.getName().getString());
        }
        return snowlette;
    }
}
