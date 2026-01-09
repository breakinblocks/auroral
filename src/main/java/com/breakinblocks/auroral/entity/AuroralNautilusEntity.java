package com.breakinblocks.auroral.entity;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.config.AuroralConfig;
import com.breakinblocks.auroral.util.AuroraHelper;
import com.breakinblocks.auroral.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Auroral Nautilus - A mystical flying nautilus creature that appears during Aurora events.
 * Can be tamed with Aurora Shards and ridden through the sky when saddled.
 */
public class AuroralNautilusEntity extends Animal implements PlayerRideable, PlayerRideableJumping {

    public static final float FLAP_DEGREES_PER_TICK = 5.0F;
    public static final int TICKS_PER_FLAP = Mth.ceil(30.0F);

    private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(AuroralNautilusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SADDLED = SynchedEntityData.defineId(AuroralNautilusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_TAMED = SynchedEntityData.defineId(AuroralNautilusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SITTING = SynchedEntityData.defineId(AuroralNautilusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_CHARGE_TIME = SynchedEntityData.defineId(AuroralNautilusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BOOST_REMAINING = SynchedEntityData.defineId(AuroralNautilusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_BOOST_X = SynchedEntityData.defineId(AuroralNautilusEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_BOOST_Y = SynchedEntityData.defineId(AuroralNautilusEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_BOOST_Z = SynchedEntityData.defineId(AuroralNautilusEntity.class, EntityDataSerializers.FLOAT);

    @Nullable
    private UUID ownerUUID;

    Vec3 moveTargetPoint = Vec3.ZERO;
    @Nullable BlockPos anchorPoint;

    private int despawnTimer = 0;
    private static final int MAX_CHARGE_TIME = 40;

    private boolean wasJumping = false;
    private int jumpChargeTime = 0;
    private int localBoostCountdown = 0;

    public AuroralNautilusEntity(EntityType<? extends AuroralNautilusEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new NautilusMoveControl(this);
        this.lookControl = new NautilusLookControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.1)
            .add(Attributes.FLYING_SPEED, 0.3);
    }

    @Override
    public boolean isFlapping() {
        return (this.getUniqueFlapTickOffset() + this.tickCount) % TICKS_PER_FLAP == 0;
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new NautilusBodyRotationControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new NautilusTemptGoal()); // Attracted to aurora shards (wild only)
        this.goalSelector.addGoal(2, new NautilusFollowOwnerGoal()); // Orbit owner when tamed
        this.goalSelector.addGoal(3, new NautilusWanderGoal()); // Default wandering
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_SIZE, 0);
        builder.define(DATA_SADDLED, false);
        builder.define(DATA_TAMED, false);
        builder.define(DATA_SITTING, false);
        builder.define(DATA_CHARGE_TIME, 0);
        builder.define(DATA_BOOST_REMAINING, 0); // 0 means no boost active
        builder.define(DATA_BOOST_X, 0.0F);
        builder.define(DATA_BOOST_Y, 0.0F);
        builder.define(DATA_BOOST_Z, 0.0F);
    }

    public int getChargeTime() {
        return this.entityData.get(DATA_CHARGE_TIME);
    }

    public void setChargeTime(int time) {
        this.entityData.set(DATA_CHARGE_TIME, time);
    }

    public int getBoostRemaining() {
        return this.entityData.get(DATA_BOOST_REMAINING);
    }

    public void setBoostRemaining(int remaining) {
        this.entityData.set(DATA_BOOST_REMAINING, remaining);
    }

    public Vec3 getBoostVelocity() {
        return new Vec3(
            this.entityData.get(DATA_BOOST_X),
            this.entityData.get(DATA_BOOST_Y),
            this.entityData.get(DATA_BOOST_Z)
        );
    }

    public void setBoostVelocity(double x, double y, double z) {
        this.entityData.set(DATA_BOOST_X, (float) x);
        this.entityData.set(DATA_BOOST_Y, (float) y);
        this.entityData.set(DATA_BOOST_Z, (float) z);
    }

    public boolean isTamed() {
        return this.entityData.get(DATA_TAMED);
    }

    public void setTamed(boolean tamed) {
        this.entityData.set(DATA_TAMED, tamed);
    }

    public boolean isSitting() {
        return this.entityData.get(DATA_SITTING);
    }

    public void setSitting(boolean sitting) {
        this.entityData.set(DATA_SITTING, sitting);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.ownerUUID = uuid;
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity instanceof Player && entity.getUUID().equals(this.getOwnerUUID());
    }

    public boolean isSaddleable() {
        return this.isAlive() && isTamed();
    }

    public void equipSaddle(ItemStack saddle, @Nullable SoundSource source) {
        this.entityData.set(DATA_SADDLED, true);
        if (source != null) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.HORSE_SADDLE, source, 0.5F, 1.0F);
        }
    }

    public boolean isSaddled() {
        return this.entityData.get(DATA_SADDLED);
    }

    public void setNautilusSize(int size) {
        this.entityData.set(ID_SIZE, Mth.clamp(size, 0, 3));
    }

    private void updateNautilusSizeInfo() {
        this.refreshDimensions();
    }

    public int getNautilusSize() {
        return this.entityData.get(ID_SIZE);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        if (ID_SIZE.equals(accessor)) {
            this.updateNautilusSizeInfo();
        }
        super.onSyncedDataUpdated(accessor);
    }

    public int getUniqueFlapTickOffset() {
        return this.getId() * 5;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Taming with Aurora Shards
        if (!isTamed() && itemStack.is(ModItems.AURORA_SHARD.get())) {
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }

            if (!this.level().isClientSide()) {
                // 33% chance to tame
                if (this.random.nextFloat() < 0.33f) {
                    this.setTamed(true);
                    this.setOwnerUUID(player.getUUID());
                    this.level().broadcastEntityEvent(this, (byte) 7); // Heart particles
                    // Tamed nautili don't despawn
                    this.despawnTimer = -999999;
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6); // Smoke particles
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Toggle sitting with shift-right-click (owner only)
        if (isTamed() && isOwnedBy(player) && player.isSecondaryUseActive()) {
            if (!this.level().isClientSide()) {
                this.setSitting(!this.isSitting());
                // Play a sound to indicate the state change
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    this.isSitting() ? SoundEvents.AMETHYST_BLOCK_PLACE : SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundSource.NEUTRAL, 0.5F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // Mounting if tamed and owner (not when sitting)
        if (isTamed() && isOwnedBy(player) && !isSitting()) {
            if (!this.level().isClientSide()) {
                player.startRiding(this);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 7) {
            // Heart particles for successful tame
            for (int i = 0; i < 7; i++) {
                double dx = this.random.nextGaussian() * 0.02;
                double dy = this.random.nextGaussian() * 0.02;
                double dz = this.random.nextGaussian() * 0.02;
                this.level().addParticle(ParticleTypes.HEART,
                    this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0),
                    dx, dy, dz);
            }
        } else if (id == 6) {
            // Smoke particles for failed tame
            for (int i = 0; i < 7; i++) {
                double dx = this.random.nextGaussian() * 0.02;
                double dy = this.random.nextGaussian() * 0.02;
                double dz = this.random.nextGaussian() * 0.02;
                this.level().addParticle(ParticleTypes.SMOKE,
                    this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0),
                    dx, dy, dz);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player player) {
            return player;
        }
        return null;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        float sizeScale = 1.0F + 0.2F * this.getNautilusSize();
        float yOffset = 1.2F * sizeScale;
        return new Vec3(0.0, yOffset, 0.0);
    }

    public float getChargeProgress() {
        return (float) getChargeTime() / MAX_CHARGE_TIME;
    }

    @Override
    public void onPlayerJump(int jumpPower) {
        // Called on client side by vanilla's jump charging system
    }

    private void applyJumpBoost(int jumpPower) {
        float chargePercent = jumpPower / 100.0F;
        float maxBoost = (float) AuroralConfig.SERVER.nautilusBoostStrength.get().doubleValue();
        float boostStrength = 1.0F + chargePercent * (maxBoost - 1.0F);

        float yaw = this.getYRot() * ((float) Math.PI / 180F);
        float pitch = this.getXRot() * ((float) Math.PI / 180F);

        double boostX = -Mth.sin(yaw) * Mth.cos(pitch) * boostStrength;
        double boostY = -Mth.sin(pitch) * boostStrength * 0.5;
        double boostZ = Mth.cos(yaw) * Mth.cos(pitch) * boostStrength;

        int boostDuration = AuroralConfig.SERVER.nautilusBoostDuration.get();
        this.setDeltaMovement(boostX, boostY, boostZ);
        this.localBoostCountdown = boostDuration;
        this.setBoostVelocity(boostX, boostY, boostZ);
        this.setBoostRemaining(boostDuration);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.NEUTRAL,
            0.8F, 1.2F + chargePercent * 0.3F);

        if (this.level() instanceof ServerLevel serverLevel) {
            int particleCount = (int) (5 + 15 * chargePercent);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                this.getX(), this.getY(), this.getZ(),
                particleCount, 0.3, 0.3, 0.3, 0.1);
        }
    }

    @Override
    public boolean canJump() {
        return this.isVehicle();
    }

    @Override
    public void handleStartJump(int jumpPower) {
        if (jumpPower > 0 && this.getBoostRemaining() <= 0) {
            applyJumpBoost(jumpPower);
        }
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        float forward = player.zza;
        float strafe = player.xxa * 0.5F;
        float pitch = player.getXRot();
        float verticalInput = 0;
        if (pitch < -20) {
            verticalInput = 0.3F;
        } else if (pitch > 20) {
            verticalInput = -0.3F;
        }
        return new Vec3(strafe, verticalInput, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) this.getAttributeValue(Attributes.FLYING_SPEED);
    }

    @Override
    protected void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);
        // Match player's yaw for steering
        this.setYRot(player.getYRot());
        this.yRotO = this.getYRot();
        this.setXRot(player.getXRot() * 0.5F);
        this.setRot(this.getYRot(), this.getXRot());
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.yBodyRot;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player player) {
            this.setYRot(player.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(player.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;

            // Use player's movement input directly (works on both client and server)
            float forward = player.zza;
            float strafe = player.xxa * 0.5F;
            // Jump is tracked via PlayerRideableJumping interface methods
            boolean isJumping = this.getChargeTime() > 0;

            int syncedBoostRemaining = this.getBoostRemaining();
            if (syncedBoostRemaining > this.localBoostCountdown) {
                this.localBoostCountdown = syncedBoostRemaining;
            }

            if (this.localBoostCountdown > 0) {
                Vec3 boostVelocity = this.getBoostVelocity();
                int boostDuration = AuroralConfig.SERVER.nautilusBoostDuration.get();
                double decayFactor = Math.pow(0.96, boostDuration + 1 - this.localBoostCountdown);
                Vec3 scaledBoost = boostVelocity.scale(decayFactor);

                this.setDeltaMovement(scaledBoost);
                this.move(MoverType.SELF, scaledBoost);
                this.localBoostCountdown--;

                if (!this.level().isClientSide()) {
                    this.setBoostRemaining(this.localBoostCountdown);
                }
                return;
            }

            float pitch = player.getXRot();
            float verticalInput = 0;
            if (forward > 0) {
                if (pitch < -20) verticalInput = 0.3F;
                else if (pitch > 20) verticalInput = -0.3F;
            }

            float chargeSlowdown = isJumping ? 0.3F : 1.0F;
            float speed = (float) this.getAttributeValue(Attributes.FLYING_SPEED) * chargeSlowdown;

            if (forward != 0 || strafe != 0 || verticalInput != 0) {
                float yaw = this.getYRot() * ((float) Math.PI / 180F);
                double moveX = (-Mth.sin(yaw) * forward + Mth.cos(yaw) * strafe) * speed;
                double moveZ = (Mth.cos(yaw) * forward + Mth.sin(yaw) * strafe) * speed;
                double moveY = verticalInput * speed;
                this.setDeltaMovement(moveX, moveY, moveZ);
            } else if (!isJumping) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91));
            return;
        }
        // Apply standard flying travel
        super.travel(travelVector);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            int remaining = this.getBoostRemaining();
            if (remaining > 0) {
                this.setBoostRemaining(remaining - 1);
            }
        }

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel && !isTamed()) {
            boolean auroraActive = AuroraHelper.isAuroraActive(serverLevel);

            if (!auroraActive) {
                despawnTimer++;
                if (despawnTimer >= AuroralConfig.SERVER.nautilusDespawnDelay.get()) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 20, 0.5, 0.5, 0.5, 0.1);
                    this.discard();
                    return;
                }
            } else {
                despawnTimer = 0;
            }
        }

        if (this.level().isClientSide()) {
            float f = Mth.cos((this.getUniqueFlapTickOffset() + this.tickCount) * FLAP_DEGREES_PER_TICK * (float) (Math.PI / 180.0) + (float) Math.PI);
            float f1 = Mth.cos((this.getUniqueFlapTickOffset() + this.tickCount + 1) * FLAP_DEGREES_PER_TICK * (float) (Math.PI / 180.0) + (float) Math.PI);

            if (f > 0.0F && f1 <= 0.0F) {
                this.level().playLocalSound(
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    this.getSoundSource(),
                    0.3F + this.random.nextFloat() * 0.1F,
                    0.8F + this.random.nextFloat() * 0.4F,
                    false
                );
            }

            if (this.tickCount % 3 == 0) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.3;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
                this.level().addParticle(
                    ParticleTypes.END_ROD,
                    this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                    0, -0.02, 0
                );
            }

            if (this.random.nextFloat() < 0.1f) {
                this.level().addParticle(
                    ParticleTypes.SNOWFLAKE,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.4,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                    0, -0.05, 0
                );
            }
        }
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Flying creature - no fall damage
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.AURORA_SHARD.get());
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        // Nautili don't breed
        return null;
    }

    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData
    ) {
        this.anchorPoint = this.blockPosition().above(5);
        this.setNautilusSize(this.random.nextInt(3));
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("anchor_pos")) {
            int[] pos = tag.getIntArray("anchor_pos");
            if (pos.length == 3) {
                this.anchorPoint = new BlockPos(pos[0], pos[1], pos[2]);
            }
        }
        this.setNautilusSize(tag.getInt("size"));
        this.setTamed(tag.getBoolean("tamed"));
        this.entityData.set(DATA_SADDLED, tag.getBoolean("saddled"));
        this.setSitting(tag.getBoolean("sitting"));
        if (tag.hasUUID("owner")) {
            this.setOwnerUUID(tag.getUUID("owner"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.anchorPoint != null) {
            tag.putIntArray("anchor_pos", new int[]{anchorPoint.getX(), anchorPoint.getY(), anchorPoint.getZ()});
        }
        tag.putInt("size", this.getNautilusSize());
        tag.putBoolean("tamed", this.isTamed());
        tag.putBoolean("saddled", this.isSaddled());
        tag.putBoolean("sitting", this.isSitting());
        if (this.getOwnerUUID() != null) {
            tag.putUUID("owner", this.getOwnerUUID());
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AMETHYST_BLOCK_RESONATE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AMETHYST_BLOCK_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_CLUSTER_BREAK;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        int size = this.getNautilusSize();
        EntityDimensions baseDimensions = super.getDefaultDimensions(pose);
        return baseDimensions.scale(1.0F + 0.2F * size);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean wasRecentlyHit) {
        super.dropCustomDeathLoot(level, source, wasRecentlyHit);

        // Drop saddle if saddled
        if (this.isSaddled()) {
            this.spawnAtLocation(new ItemStack(Items.SADDLE));
        }

        // Only drop Aurora Shards if killed by a player
        if (source.getEntity() instanceof Player) {
            int shardCount = 1 + this.random.nextInt(2);
            for (int i = 0; i < shardCount; i++) {
                this.spawnAtLocation(new ItemStack(ModItems.AURORA_SHARD.get()));
            }

            // Rare chance to drop a nautilus shell
            if (this.random.nextFloat() < 0.15f) {
                this.spawnAtLocation(new ItemStack(net.minecraft.world.item.Items.NAUTILUS_SHELL));
            }
        }
    }

    @Override
    public boolean canBeLeashed() {
        return isTamed();
    }

    @Override
    public boolean isPersistenceRequired() {
        return isTamed() || super.isPersistenceRequired();
    }

    // Inner Classes for AI

    class NautilusBodyRotationControl extends BodyRotationControl {
        public NautilusBodyRotationControl(Mob mob) {
            super(mob);
        }

        @Override
        public void clientTick() {
            AuroralNautilusEntity.this.yHeadRot = AuroralNautilusEntity.this.yBodyRot;
            AuroralNautilusEntity.this.yBodyRot = AuroralNautilusEntity.this.getYRot();
        }
    }

    static class NautilusLookControl extends LookControl {
        public NautilusLookControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            // Nautilus doesn't need active look control
        }
    }

    class NautilusMoveControl extends MoveControl {
        private float speed = 0.05F;

        public NautilusMoveControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            // Don't use AI movement when being ridden
            if (AuroralNautilusEntity.this.isVehicle()) {
                return;
            }

            // When sitting, hover in place with minimal movement
            if (AuroralNautilusEntity.this.isSitting()) {
                // Slowly reduce velocity to hover
                Vec3 currentVel = AuroralNautilusEntity.this.getDeltaMovement();
                AuroralNautilusEntity.this.setDeltaMovement(currentVel.scale(0.9));
                return;
            }

            if (AuroralNautilusEntity.this.horizontalCollision) {
                AuroralNautilusEntity.this.setYRot(AuroralNautilusEntity.this.getYRot() + 180.0F);
                this.speed = 0.05F;
            }

            double dx = AuroralNautilusEntity.this.moveTargetPoint.x - AuroralNautilusEntity.this.getX();
            double dy = AuroralNautilusEntity.this.moveTargetPoint.y - AuroralNautilusEntity.this.getY();
            double dz = AuroralNautilusEntity.this.moveTargetPoint.z - AuroralNautilusEntity.this.getZ();
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);

            if (Math.abs(horizontalDist) > 1.0E-5F) {
                double d4 = 1.0 - Math.abs(dy * 0.7F) / horizontalDist;
                dx *= d4;
                dz *= d4;
                horizontalDist = Math.sqrt(dx * dx + dz * dz);
                double totalDist = Math.sqrt(dx * dx + dz * dz + dy * dy);

                float currentYaw = AuroralNautilusEntity.this.getYRot();
                float targetYaw = (float) Mth.atan2(dz, dx);
                float wrappedCurrentYaw = Mth.wrapDegrees(AuroralNautilusEntity.this.getYRot() + 90.0F);
                float wrappedTargetYaw = Mth.wrapDegrees(targetYaw * (180.0F / (float) Math.PI));

                // Slower, more graceful rotation
                AuroralNautilusEntity.this.setYRot(Mth.approachDegrees(wrappedCurrentYaw, wrappedTargetYaw, 2.0F) - 90.0F);
                AuroralNautilusEntity.this.yBodyRot = AuroralNautilusEntity.this.getYRot();

                if (Mth.degreesDifferenceAbs(currentYaw, AuroralNautilusEntity.this.getYRot()) < 3.0F) {
                    this.speed = Mth.approach(this.speed, 0.8F, 0.003F * (0.8F / this.speed));
                } else {
                    this.speed = Mth.approach(this.speed, 0.1F, 0.015F);
                }

                float pitch = (float) (-(Mth.atan2(-dy, horizontalDist) * 180.0F / (float) Math.PI));
                AuroralNautilusEntity.this.setXRot(pitch);

                float yawRad = AuroralNautilusEntity.this.getYRot() + 90.0F;
                double vx = this.speed * Mth.cos(yawRad * (float) (Math.PI / 180.0)) * Math.abs(dx / totalDist);
                double vy = this.speed * Mth.sin(pitch * (float) (Math.PI / 180.0)) * Math.abs(dy / totalDist);
                double vz = this.speed * Mth.sin(yawRad * (float) (Math.PI / 180.0)) * Math.abs(dz / totalDist);

                Vec3 currentVel = AuroralNautilusEntity.this.getDeltaMovement();
                AuroralNautilusEntity.this.setDeltaMovement(currentVel.add(new Vec3(vx, vy, vz).subtract(currentVel).scale(0.1)));
            }
        }
    }

    /**
     * Wander goal - makes the nautilus drift around peacefully
     */
    class NautilusWanderGoal extends Goal {
        private float angle;
        private float distance;
        private float height;
        private float direction;

        public NautilusWanderGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            // Don't wander when being ridden, sitting, or when tamed (follow owner handles that)
            if (AuroralNautilusEntity.this.isVehicle()) return false;
            if (AuroralNautilusEntity.this.isSitting()) return false;
            if (AuroralNautilusEntity.this.isTamed()) return false;
            return true;
        }

        @Override
        public void start() {
            this.distance = 4.0F + AuroralNautilusEntity.this.random.nextFloat() * 8.0F;
            this.height = -2.0F + AuroralNautilusEntity.this.random.nextFloat() * 6.0F;
            this.direction = AuroralNautilusEntity.this.random.nextBoolean() ? 1.0F : -1.0F;
            this.selectNextTarget();
        }

        @Override
        public void tick() {
            // Occasionally change height
            if (AuroralNautilusEntity.this.random.nextInt(adjustedTickDelay(500)) == 0) {
                this.height = -2.0F + AuroralNautilusEntity.this.random.nextFloat() * 6.0F;
            }

            // Occasionally change distance
            if (AuroralNautilusEntity.this.random.nextInt(adjustedTickDelay(400)) == 0) {
                this.distance += 1.0F;
                if (this.distance > 12.0F) {
                    this.distance = 4.0F;
                    this.direction = -this.direction;
                }
            }

            // Occasionally change angle randomly
            if (AuroralNautilusEntity.this.random.nextInt(adjustedTickDelay(600)) == 0) {
                this.angle = AuroralNautilusEntity.this.random.nextFloat() * 2.0F * (float) Math.PI;
                this.selectNextTarget();
            }

            // Check if reached target
            if (touchingTarget()) {
                this.selectNextTarget();
            }

            // Avoid going too low
            if (AuroralNautilusEntity.this.moveTargetPoint.y < AuroralNautilusEntity.this.getY()
                && !AuroralNautilusEntity.this.level().isEmptyBlock(AuroralNautilusEntity.this.blockPosition().below(1))) {
                this.height = Math.max(1.0F, this.height);
                this.selectNextTarget();
            }

            // Avoid going into solid blocks above
            if (AuroralNautilusEntity.this.moveTargetPoint.y > AuroralNautilusEntity.this.getY()
                && !AuroralNautilusEntity.this.level().isEmptyBlock(AuroralNautilusEntity.this.blockPosition().above(1))) {
                this.height = Math.min(-1.0F, this.height);
                this.selectNextTarget();
            }
        }

        private boolean touchingTarget() {
            return AuroralNautilusEntity.this.moveTargetPoint.distanceToSqr(
                AuroralNautilusEntity.this.getX(),
                AuroralNautilusEntity.this.getY(),
                AuroralNautilusEntity.this.getZ()
            ) < 4.0;
        }

        private void selectNextTarget() {
            if (AuroralNautilusEntity.this.anchorPoint == null) {
                AuroralNautilusEntity.this.anchorPoint = AuroralNautilusEntity.this.blockPosition();
            }

            // Slower angular movement for graceful drifting
            this.angle = this.angle + this.direction * 10.0F * (float) (Math.PI / 180.0);
            AuroralNautilusEntity.this.moveTargetPoint = Vec3.atLowerCornerOf(AuroralNautilusEntity.this.anchorPoint)
                .add(this.distance * Mth.cos(this.angle), -2.0F + this.height, this.distance * Mth.sin(this.angle));
        }
    }

    /**
     * Tempt goal - wild nautili are attracted to players holding Aurora Shards
     */
    class NautilusTemptGoal extends Goal {
        private static final double TEMPT_RANGE = 10.0;
        private static final double CLOSE_ENOUGH_DIST_SQ = 6.25; // 2.5 blocks squared
        @Nullable
        private Player temptingPlayer;
        private int calmDown;

        public NautilusTemptGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            // Only for wild nautili
            if (AuroralNautilusEntity.this.isTamed()) return false;
            if (AuroralNautilusEntity.this.isVehicle()) return false;
            if (this.calmDown > 0) {
                this.calmDown--;
                return false;
            }

            this.temptingPlayer = findTemptingPlayer();
            return this.temptingPlayer != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (AuroralNautilusEntity.this.isTamed()) return false;

            if (this.temptingPlayer == null || !this.temptingPlayer.isAlive()) return false;

            double distSq = AuroralNautilusEntity.this.distanceToSqr(this.temptingPlayer);
            if (distSq > TEMPT_RANGE * TEMPT_RANGE) return false;

            return isHoldingTemptItem(this.temptingPlayer);
        }

        @Override
        public void start() {
            // Set initial target toward the player
            if (this.temptingPlayer != null) {
                updateTargetPoint();
            }
        }

        @Override
        public void stop() {
            this.temptingPlayer = null;
            this.calmDown = 100; // Wait 5 seconds before trying again
        }

        @Override
        public void tick() {
            if (this.temptingPlayer == null) return;

            // Look toward the player
            AuroralNautilusEntity nautilus = AuroralNautilusEntity.this;
            nautilus.getLookControl().setLookAt(this.temptingPlayer, 30.0F, 30.0F);

            // Move closer if not close enough
            double distSq = nautilus.distanceToSqr(this.temptingPlayer);
            if (distSq > CLOSE_ENOUGH_DIST_SQ) {
                updateTargetPoint();
            } else {
                // Close enough - hover near the player
                nautilus.moveTargetPoint = new Vec3(
                    nautilus.getX(),
                    nautilus.getY(),
                    nautilus.getZ()
                );
            }
        }

        private void updateTargetPoint() {
            if (this.temptingPlayer == null) return;

            AuroralNautilusEntity nautilus = AuroralNautilusEntity.this;
            // Target a point above the player's head
            nautilus.moveTargetPoint = this.temptingPlayer.position().add(0, 1.5, 0);
            nautilus.anchorPoint = this.temptingPlayer.blockPosition().above(2);
        }

        @Nullable
        private Player findTemptingPlayer() {
            AuroralNautilusEntity nautilus = AuroralNautilusEntity.this;
            double x = nautilus.getX();
            double y = nautilus.getY();
            double z = nautilus.getZ();

            Player nearest = null;
            double nearestDistSq = TEMPT_RANGE * TEMPT_RANGE;

            for (Player player : nautilus.level().players()) {
                if (!isHoldingTemptItem(player)) continue;

                double distSq = player.distanceToSqr(x, y, z);
                if (distSq < nearestDistSq) {
                    nearestDistSq = distSq;
                    nearest = player;
                }
            }

            return nearest;
        }

        private boolean isHoldingTemptItem(Player player) {
            return player.getMainHandItem().is(ModItems.AURORA_SHARD.get()) ||
                   player.getOffhandItem().is(ModItems.AURORA_SHARD.get());
        }
    }

    /**
     * Follow owner goal - tamed nautili slowly orbit around their owner
     */
    class NautilusFollowOwnerGoal extends Goal {
        private static final double MAX_DIST = 20.0; // Start following if further than this
        private static final double ORBIT_RADIUS = 3.0; // Orbit radius around owner
        private static final double ORBIT_HEIGHT = 2.0; // Height above owner's head
        private static final float ORBIT_SPEED = 0.02F; // Radians per tick (slow orbit)

        @Nullable
        private Player owner;
        private float orbitAngle;
        private int ticksSinceOwnerSeen;

        public NautilusFollowOwnerGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
            this.orbitAngle = (float) (Math.random() * Math.PI * 2); // Random start angle
        }

        @Override
        public boolean canUse() {
            if (!AuroralNautilusEntity.this.isTamed()) return false;
            if (AuroralNautilusEntity.this.isSitting()) return false;
            if (AuroralNautilusEntity.this.isVehicle()) return false;

            this.owner = findOwner();
            return this.owner != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (!AuroralNautilusEntity.this.isTamed()) return false;
            if (AuroralNautilusEntity.this.isSitting()) return false;
            if (AuroralNautilusEntity.this.isVehicle()) return false;
            if (this.owner == null || !this.owner.isAlive()) return false;

            // Stop if owner is too far for too long
            double distSq = AuroralNautilusEntity.this.distanceToSqr(this.owner);
            if (distSq > MAX_DIST * MAX_DIST) {
                this.ticksSinceOwnerSeen++;
                if (this.ticksSinceOwnerSeen > 200) { // 10 seconds
                    return false;
                }
            } else {
                this.ticksSinceOwnerSeen = 0;
            }

            return true;
        }

        @Override
        public void start() {
            this.ticksSinceOwnerSeen = 0;
        }

        @Override
        public void stop() {
            this.owner = null;
        }

        @Override
        public void tick() {
            if (this.owner == null) return;

            AuroralNautilusEntity nautilus = AuroralNautilusEntity.this;

            // Slowly increment orbit angle
            this.orbitAngle += ORBIT_SPEED;
            if (this.orbitAngle > Math.PI * 2) {
                this.orbitAngle -= (float) (Math.PI * 2);
            }

            // Calculate orbit position around owner
            double targetX = this.owner.getX() + ORBIT_RADIUS * Mth.cos(this.orbitAngle);
            double targetY = this.owner.getY() + this.owner.getEyeHeight() + ORBIT_HEIGHT;
            double targetZ = this.owner.getZ() + ORBIT_RADIUS * Mth.sin(this.orbitAngle);

            nautilus.moveTargetPoint = new Vec3(targetX, targetY, targetZ);
            nautilus.anchorPoint = new BlockPos((int) targetX, (int) targetY, (int) targetZ);
        }

        @Nullable
        private Player findOwner() {
            UUID ownerUUID = AuroralNautilusEntity.this.getOwnerUUID();
            if (ownerUUID == null) return null;

            // Search for owner in loaded players
            if (AuroralNautilusEntity.this.level() instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(ownerUUID);
                if (entity instanceof Player player) {
                    return player;
                }
            }

            // Fallback: search nearby players
            for (Player player : AuroralNautilusEntity.this.level().players()) {
                if (player.getUUID().equals(ownerUUID) && player.distanceToSqr(AuroralNautilusEntity.this) < MAX_DIST * MAX_DIST) {
                    return player;
                }
            }

            return null;
        }
    }
}
