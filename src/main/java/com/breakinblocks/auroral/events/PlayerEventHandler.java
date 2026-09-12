package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.config.AuroralConfig;
import com.breakinblocks.auroral.entity.AuroralNautilusEntity;
import com.breakinblocks.auroral.net.AuroralNetworking;
import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.registry.ModEntities;
import com.breakinblocks.auroral.registry.ModTags;
import com.breakinblocks.auroral.util.AuroraHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Auroral.MOD_ID)
public class PlayerEventHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Sync aurora state to the joining player
            AuroralNetworking.syncAuroraToPlayer(player);
        }
    }

    /** Position of each player just before a dimension change, so we can find pets nearby. */
    private static final Map<UUID, Vec3> preTransitPositions = new HashMap<>();
    private static final double NAUTILUS_FOLLOW_RADIUS = 32.0;

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            preTransitPositions.put(player.getUUID(), player.position());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AuroralNetworking.syncAuroraToPlayer(player);
            bringTamedNautili(player, event.getFrom());
            preTransitPositions.remove(player.getUUID());
        }
    }

    private static void bringTamedNautili(ServerPlayer player, ResourceKey<Level> fromKey) {
        MinecraftServer server = player.level().getServer();
        if (server == null) return;
        ServerLevel sourceLevel = server.getLevel(fromKey);
        ServerLevel destLevel = player.level();
        if (sourceLevel == null || sourceLevel == destLevel) return;

        UUID playerId = player.getUUID();
        Vec3 lastPos = preTransitPositions.get(playerId);
        double radiusSq = NAUTILUS_FOLLOW_RADIUS * NAUTILUS_FOLLOW_RADIUS;

        List<? extends AuroralNautilusEntity> followers = sourceLevel.getEntities(
            ModEntities.AURORAL_NAUTILUS.get(),
            n -> n.isTamed()
                && playerId.equals(n.getOwnerUUID())
                && (lastPos == null || n.position().distanceToSqr(lastPos) <= radiusSq)
        );

        for (AuroralNautilusEntity nautilus : followers) {
            nautilus.stopRiding();
            nautilus.ejectPassengers();
            nautilus.teleport(new TeleportTransition(
                destLevel,
                player.position(),
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                TeleportTransition.DO_NOTHING
            ));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AuroralNetworking.syncAuroraToPlayer(player);
        }
    }

    /**
     * Aurora Lantern grants darkness immunity when held in either hand.
     * Also handles aurora self-repair for Shimmersteel/Shimmerweave gear.
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

        // Aurora self-repair - once per second during aurora
        if (player.tickCount % 20 == 0 && AuroraHelper.isExperiencingAurora(player.level(), player.blockPosition())) {
            repairAuroraGear(player);
        }
    }

    private static void repairAuroraGear(Player player) {
        int repairAmount = AuroralConfig.SERVER.auroraRepairRate.get();
        if (repairAmount <= 0) {
            return;
        }

        // Repair equipped armor
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack stack = player.getItemBySlot(slot);
                tryRepairItem(stack, repairAmount);
            }
        }

        tryRepairItem(player.getOffhandItem(), repairAmount);

        // Repair items in hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            tryRepairItem(stack, repairAmount);
        }
    }

    private static void tryRepairItem(ItemStack stack, int repairAmount) {
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return;
        }

        if (!stack.is(ModTags.Items.AURORA_SELF_REPAIR)) {
            return;
        }

        int damage = stack.getDamageValue();
        if (damage > 0) {
            stack.setDamageValue(Math.max(0, damage - repairAmount));
        }
    }
}
