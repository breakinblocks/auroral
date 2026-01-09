package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.config.AuroralConfig;
import com.breakinblocks.auroral.registry.ModBlockEntities;
import com.breakinblocks.auroral.util.AuroraHelper;
import com.breakinblocks.auroral.util.BiomeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the Glacial Basin.
 * Handles aura collection during aurora events.
 *
 * Note: Aura level is stored in the block state (single source of truth).
 * This BlockEntity only tracks the fill tick counter for aura collection timing.
 */
public class GlacialBasinBlockEntity extends BlockEntity {
    private int fillTickCounter = 0;

    public GlacialBasinBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GLACIAL_BASIN.get(), pos, state);
    }

    /**
     * Server-side tick handler.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, GlacialBasinBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Only collect aura during aurora in cold biomes
        if (!AuroraHelper.isAuroraActive(serverLevel)) {
            return;
        }

        if (!BiomeHelper.isColdBiome(level, pos)) {
            return;
        }

        int maxAura = AuroralConfig.SERVER.basinMaxAura.get();
        int currentAura = state.getValue(GlacialBasinBlock.AURA_LEVEL);

        // Already full
        if (currentAura >= maxAura) {
            return;
        }

        // Increment fill counter
        blockEntity.fillTickCounter++;

        int fillRate = AuroralConfig.SERVER.basinFillRate.get();
        if (blockEntity.fillTickCounter >= fillRate) {
            blockEntity.fillTickCounter = 0;

            // Add one aura level
            int newLevel = Math.min(currentAura + 1, maxAura);
            level.setBlock(pos, state.setValue(GlacialBasinBlock.AURA_LEVEL, newLevel), 3);
            blockEntity.setChanged();
        }
    }

    /**
     * Gets the current aura level from the block state (single source of truth).
     */
    public int getAuraLevel() {
        BlockState state = getBlockState();
        if (state.hasProperty(GlacialBasinBlock.AURA_LEVEL)) {
            return state.getValue(GlacialBasinBlock.AURA_LEVEL);
        }
        return 0;
    }

    /**
     * Sets the aura level by updating the block state.
     */
    public void setAuraLevel(int newLevel) {
        if (level != null) {
            BlockState state = getBlockState();
            level.setBlock(worldPosition, state.setValue(GlacialBasinBlock.AURA_LEVEL, newLevel), 3);
            setChanged();
        }
    }

    /**
     * Checks if the basin has any aura.
     */
    public boolean hasAura() {
        return getAuraLevel() > 0;
    }

    /**
     * Consumes one level of aura from the basin.
     */
    public void consumeAura() {
        int currentLevel = getAuraLevel();
        if (currentLevel > 0) {
            setAuraLevel(currentLevel - 1);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("FillCounter", fillTickCounter);
        // Note: AuraLevel is stored in block state, not BlockEntity
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fillTickCounter = tag.getInt("FillCounter");
        // Note: AuraLevel is stored in block state, not BlockEntity
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Note: The saveAdditional method is used for both persistent storage and update tags in 1.21.11
    // The auraLevel is already saved in saveAdditional, so it will be synced properly
}
