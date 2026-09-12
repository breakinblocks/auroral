package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.config.AuroralConfig;
import com.breakinblocks.auroral.registry.ModBlockEntities;
import com.breakinblocks.auroral.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HearthwoodLogBlockEntity extends BlockEntity {

    private static final int EFFECT_INTERVAL = 40; // 2 seconds
    private static final int EFFECT_DURATION = 60; // 3 seconds

    private int burnTimeRemaining = getMaxBurnTime();
    private int effectTimer = 0;

    public HearthwoodLogBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEARTHWOOD_LOG.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HearthwoodLogBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!state.getValue(HearthwoodLogBlock.LIT)) {
            return;
        }

        blockEntity.burnTimeRemaining--;

        if (blockEntity.burnTimeRemaining <= 0) {
            level.setBlock(pos, state.setValue(HearthwoodLogBlock.LIT, false), 3);
            blockEntity.setChanged();
            return;
        }

        blockEntity.effectTimer++;
        if (blockEntity.effectTimer >= EFFECT_INTERVAL) {
            blockEntity.effectTimer = 0;
            blockEntity.applyEffectsToNearbyPlayers(serverLevel, pos);
            blockEntity.igniteNearbyPhantoms(serverLevel, pos);
        }

        if (blockEntity.burnTimeRemaining % 1200 == 0) {
            blockEntity.setChanged();
        }
    }

    public static int getMaxBurnTime() {
        return AuroralConfig.SERVER.hearthwoodLogBurnTime.get();
    }

    public static double getEffectRadius() {
        return AuroralConfig.SERVER.hearthwoodLogFrostbiteRadius.get();
    }

    private void igniteNearbyPhantoms(ServerLevel level, BlockPos pos) {
        AABB effectBox = new AABB(pos).inflate(getEffectRadius());
        List<Phantom> phantoms = level.getEntitiesOfClass(Phantom.class, effectBox);
        for (Phantom phantom : phantoms) {
            phantom.igniteForSeconds(5.0f);
        }
    }

    private void applyEffectsToNearbyPlayers(ServerLevel level, BlockPos pos) {
        AABB effectBox = new AABB(pos).inflate(getEffectRadius());
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, effectBox);

        for (Player player : nearbyPlayers) {
            player.addEffect(new MobEffectInstance(
                ModEffects.FROSTBITE_IMMUNITY,
                EFFECT_DURATION,
                0,
                true,
                false,
                true
            ));
        }
    }

    public void resetBurnTime() {
        this.burnTimeRemaining = getMaxBurnTime();
        this.effectTimer = 0;
        this.setChanged();
    }

    public int getBurnTimeRemaining() {
        return burnTimeRemaining;
    }

    public float getBurnProgress() {
        return (float) burnTimeRemaining / getMaxBurnTime();
    }

    public boolean isLit() {
        return burnTimeRemaining > 0 && getBlockState().getValue(HearthwoodLogBlock.LIT);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("BurnTime", burnTimeRemaining);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        burnTimeRemaining = input.getIntOr("BurnTime", getMaxBurnTime());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
