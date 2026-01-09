package com.breakinblocks.auroral.net;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.util.AuroraHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Handles network payload registration and sending utilities.
 */
public class AuroralNetworking {

    /**
     * Registers networking handlers on the MOD event bus.
     * Must be called from the main mod constructor.
     *
     * @param eventBus The mod event bus
     */
    public static void register(IEventBus eventBus) {
        eventBus.addListener(AuroralNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Auroral.MOD_ID);

        // Server to client: Aurora state sync
        registrar.playToClient(
            SyncAuroraPayload.TYPE,
            SyncAuroraPayload.STREAM_CODEC,
            SyncAuroraPayload::handleOnClient
        );
    }

    /**
     * Syncs aurora state to all players in the given level.
     *
     * @param level The server level
     * @param active Whether aurora is active
     */
    public static void syncAuroraToAll(ServerLevel level, boolean active) {
        SyncAuroraPayload payload = new SyncAuroraPayload(active);
        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    /**
     * Syncs aurora state to a specific player.
     * Called when a player joins or changes dimension.
     *
     * @param player The player to sync to
     */
    public static void syncAuroraToPlayer(ServerPlayer player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            boolean active = AuroraHelper.isAuroraActive(serverLevel);
            PacketDistributor.sendToPlayer(player, new SyncAuroraPayload(active));
        }
    }
}
