package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.registry.ModBlockEntities;
import com.breakinblocks.auroral.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Block entity for the Hearthwood Log.
 * Tracks burn time and applies effects to nearby players.
 */
public class HearthwoodLogBlockEntity extends BlockEntity {

    /**
     * Total burn time: 7 days = 7 * 24000 ticks = 168000 ticks
     */
    public static final int MAX_BURN_TIME = 168000;

    /**
     * Effect radius in blocks
     */
    public static final double EFFECT_RADIUS = 16.0;

    /**
     * How often to apply effects (every 2 seconds)
     */
    private static final int EFFECT_INTERVAL = 40;

    /**
     * Duration of immunity effect (3 seconds, refreshed every 2)
     */
    private static final int EFFECT_DURATION = 60;

    private int burnTimeRemaining = MAX_BURN_TIME;
    private int effectTimer = 0;

    public HearthwoodLogBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEARTHWOOD_LOG.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HearthwoodLogBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Only tick if lit
        if (!state.getValue(HearthwoodLogBlock.LIT)) {
            return;
        }

        // Decrease burn time
        blockEntity.burnTimeRemaining--;

        // Check if burned out
        if (blockEntity.burnTimeRemaining <= 0) {
            // Extinguish the log
            level.setBlock(pos, state.setValue(HearthwoodLogBlock.LIT, false), 3);
            blockEntity.setChanged();
            return;
        }

        // Apply effects periodically
        blockEntity.effectTimer++;
        if (blockEntity.effectTimer >= EFFECT_INTERVAL) {
            blockEntity.effectTimer = 0;
            blockEntity.applyEffectsToNearbyPlayers(serverLevel, pos);
        }

        // Mark changed occasionally for saving
        if (blockEntity.burnTimeRemaining % 1200 == 0) { // Every minute
            blockEntity.setChanged();
        }
    }

    private void applyEffectsToNearbyPlayers(ServerLevel level, BlockPos pos) {
        AABB effectBox = new AABB(pos).inflate(EFFECT_RADIUS);
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, effectBox);

        for (Player player : nearbyPlayers) {
            // Grant Frostbite immunity (by giving resistance to our Frostbite effect)
            // We'll use Fire Resistance as a proxy since it thematically makes sense
            // for a warm fire, and also add our custom immunity via the event handler
            player.addEffect(new MobEffectInstance(
                ModEffects.FROSTBITE_IMMUNITY,
                EFFECT_DURATION,
                0,
                true,  // ambient
                false, // visible
                true   // show icon
            ));
        }
    }

    public int getBurnTimeRemaining() {
        return burnTimeRemaining;
    }

    public float getBurnProgress() {
        return (float) burnTimeRemaining / MAX_BURN_TIME;
    }

    public boolean isLit() {
        return burnTimeRemaining > 0 && getBlockState().getValue(HearthwoodLogBlock.LIT);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("BurnTime", burnTimeRemaining);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        burnTimeRemaining = tag.contains("BurnTime") ? tag.getInt("BurnTime") : MAX_BURN_TIME;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
