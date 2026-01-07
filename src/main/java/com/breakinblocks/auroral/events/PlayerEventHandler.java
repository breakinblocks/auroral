package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.net.AuroralNetworking;
import com.breakinblocks.auroral.registry.ModBlocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Handles player-related events.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID)
public class PlayerEventHandler {

    /**
     * Sync aurora state when a player logs in.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Sync aurora state to the joining player
            AuroralNetworking.syncAuroraToPlayer(player);
        }
    }

    /**
     * Sync aurora state when a player changes dimension.
     */
    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Sync aurora state for the new dimension
            AuroralNetworking.syncAuroraToPlayer(player);
        }
    }

    /**
     * Sync aurora state when a player respawns.
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AuroralNetworking.syncAuroraToPlayer(player);
        }
    }

    /**
     * Aurora Lantern grants darkness immunity when held in either hand.
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        // Check if player is holding Aurora Lantern in either hand
        boolean holdingLantern = player.getMainHandItem().is(ModBlocks.AURORA_LANTERN.asItem()) ||
                                  player.getOffhandItem().is(ModBlocks.AURORA_LANTERN.asItem());

        if (holdingLantern && player.hasEffect(MobEffects.DARKNESS)) {
            player.removeEffect(MobEffects.DARKNESS);
        }
    }
}
