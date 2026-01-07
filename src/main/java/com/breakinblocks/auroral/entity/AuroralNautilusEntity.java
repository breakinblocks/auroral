package com.breakinblocks.auroral.entity;

import com.breakinblocks.auroral.util.AuroraHelper;
import com.breakinblocks.auroral.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Auroral Nautilus - A mystical flying nautilus creature that appears during Aurora events.
 * Can be tamed with Aurora Shards and ridden through the sky when saddled.
 */
public class AuroralNautilusEntity extends Animal implements PlayerRideable {

    public static final float FLAP_DEGREES_PER_TICK = 5.0F;
    public static final int TICKS_PER_FLAP = Mth.ceil(30.0F);

    private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(AuroralNautilusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SADDLED = SynchedEntityData.defineId(AuroralNautilusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_TAMED = SynchedEntityData.defineId(AuroralNautilusEntity.class, EntityDataSerializers.BOOLEAN);

    // Owner UUID stored in save data, not synced
    @Nullable
    private UUID ownerUUID;

    Vec3 moveTargetPoint = Vec3.ZERO;
    @Nullable BlockPos anchorPoint;

    // Despawn timer when aurora ends (only for wild nautili)
    private int despawnTimer = 0;
    private static final int DESPAWN_DELAY = 600; // 30 seconds after aurora ends

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
        this.goalSelector.addGoal(1, new NautilusWanderGoal());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_SIZE, 0);
        builder.define(DATA_SADDLED, false);
        builder.define(DATA_TAMED, false);
    }

    // Taming methods
    public boolean isTamed() {
        return this.entityData.get(DATA_TAMED);
    }

    public void setTamed(boolean tamed) {
        this.entityData.set(DATA_TAMED, tamed);
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

    // Saddle methods
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

        // Saddling
        if (isTamed() && !isSaddled() && itemStack.is(Items.SADDLE)) {
            this.equipSaddle(itemStack, SoundSource.NEUTRAL);
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        // Mounting if tamed, saddled, and owner
        if (isTamed() && isSaddled() && isOwnedBy(player)) {
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
        if (this.isSaddled()) {
            Entity entity = this.getFirstPassenger();
            if (entity instanceof Player player && this.isOwnedBy(player)) {
                return player;
            }
        }
        return null;
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        // Get player's look direction for flying control
        float forward = player.zza;
        float strafe = player.xxa * 0.5F;

        // Looking up/down controls vertical movement
        float pitch = player.getXRot();
        float verticalInput = 0;
        if (pitch < -20) {
            verticalInput = 0.3F; // Looking up = fly up
        } else if (pitch > 20) {
            verticalInput = -0.3F; // Looking down = fly down
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
        if (this.isVehicle() && this.getControllingPassenger() != null) {
            // Flying movement when ridden
            LivingEntity passenger = this.getControllingPassenger();
            if (passenger instanceof Player player) {
                Vec3 input = this.getRiddenInput(player, travelVector);
                float speed = this.getRiddenSpeed(player);

                // Apply movement based on look direction
                float yaw = this.getYRot() * ((float) Math.PI / 180F);
                double moveX = -Mth.sin(yaw) * input.z * speed + Mth.cos(yaw) * input.x * speed;
                double moveZ = Mth.cos(yaw) * input.z * speed + Mth.sin(yaw) * input.x * speed;
                double moveY = input.y * speed;

                this.setDeltaMovement(moveX, moveY, moveZ);
                this.move(MoverType.SELF, this.getDeltaMovement());

                // Slow down over time
                this.setDeltaMovement(this.getDeltaMovement().scale(0.91));
            }
        } else {
            // Normal AI movement
            this.travelFlying(travelVector, 0.15F);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Check aurora state and manage despawn (only for wild nautili)
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel && !isTamed()) {
            boolean auroraActive = AuroraHelper.isAuroraActive(serverLevel);

            if (!auroraActive) {
                despawnTimer++;
                if (despawnTimer >= DESPAWN_DELAY) {
                    // Gracefully fade away with particles
                    serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 20, 0.5, 0.5, 0.5, 0.1);
                    this.discard();
                    return;
                }
            } else {
                despawnTimer = 0;
            }
        }

        // Client-side particle effects
        if (this.level().isClientSide()) {
            float f = Mth.cos((this.getUniqueFlapTickOffset() + this.tickCount) * FLAP_DEGREES_PER_TICK * (float) (Math.PI / 180.0) + (float) Math.PI);
            float f1 = Mth.cos((this.getUniqueFlapTickOffset() + this.tickCount + 1) * FLAP_DEGREES_PER_TICK * (float) (Math.PI / 180.0) + (float) Math.PI);

            // Play ambient sound on flap
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

            // Aurora trail particles
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

            // Occasional snowflake particles
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
        ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData spawnData
    ) {
        this.anchorPoint = this.blockPosition().above(5);
        this.setNautilusSize(this.random.nextInt(3));
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.anchorPoint = input.read("anchor_pos", BlockPos.CODEC).orElse(null);
        this.setNautilusSize(input.getIntOr("size", 0));
        this.setTamed(input.getBooleanOr("tamed", false));
        this.entityData.set(DATA_SADDLED, input.getBooleanOr("saddled", false));
        input.read("owner", net.minecraft.core.UUIDUtil.CODEC).ifPresent(this::setOwnerUUID);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.storeNullable("anchor_pos", BlockPos.CODEC, this.anchorPoint);
        output.putInt("size", this.getNautilusSize());
        output.putBoolean("tamed", this.isTamed());
        output.putBoolean("saddled", this.isSaddled());
        if (this.getOwnerUUID() != null) {
            output.store("owner", net.minecraft.core.UUIDUtil.CODEC, this.getOwnerUUID());
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
            this.spawnAtLocation(level, new ItemStack(Items.SADDLE));
        }

        // Only drop Aurora Shards if killed by a player
        if (source.getEntity() instanceof Player) {
            int shardCount = 1 + this.random.nextInt(2);
            for (int i = 0; i < shardCount; i++) {
                this.spawnAtLocation(level, new ItemStack(ModItems.AURORA_SHARD.get()));
            }

            // Rare chance to drop a nautilus shell
            if (this.random.nextFloat() < 0.15f) {
                this.spawnAtLocation(level, new ItemStack(net.minecraft.world.item.Items.NAUTILUS_SHELL));
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
            // Don't wander when being ridden or when tamed and owner nearby
            return !AuroralNautilusEntity.this.isVehicle();
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
}
