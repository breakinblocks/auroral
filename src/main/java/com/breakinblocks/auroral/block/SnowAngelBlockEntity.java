package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for Snow Angel that tracks age and permanence.
 * Snow angels fade away after 5 minutes unless preserved with a Frozen Petal.
 */
public class SnowAngelBlockEntity extends BlockEntity {

    /**
     * Time until the snow angel fades: 5 minutes = 6000 ticks
     */
    public static final int FADE_TIME = 6000;

    private int age = 0;
    private boolean permanent = false;

    public SnowAngelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SNOW_ANGEL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SnowAngelBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Don't age if permanent
        if (blockEntity.permanent) {
            return;
        }

        blockEntity.age++;

        // Fade away when time is up
        if (blockEntity.age >= FADE_TIME) {
            // Spawn particles before removing
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5,
                10, 0.5, 0.1, 0.5, 0.02);

            // Remove the block without drops
            level.removeBlock(pos, false);
        }
    }

    /**
     * Makes this snow angel permanent (won't fade).
     */
    public void makePermanent() {
        this.permanent = true;
        this.setChanged();
    }

    /**
     * Checks if this snow angel is permanent.
     */
    public boolean isPermanent() {
        return permanent;
    }

    /**
     * Gets the current age in ticks.
     */
    public int getAge() {
        return age;
    }

    /**
     * Gets the fade progress (0.0 = fresh, 1.0 = about to fade).
     */
    public float getFadeProgress() {
        if (permanent) return 0.0f;
        return (float) age / FADE_TIME;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Age", age);
        output.putBoolean("Permanent", permanent);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        age = input.getIntOr("Age", 0);
        permanent = input.getBooleanOr("Permanent", false);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
