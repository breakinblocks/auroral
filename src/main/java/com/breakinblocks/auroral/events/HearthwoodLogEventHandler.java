package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.block.HearthwoodLogBlockEntity;
import com.breakinblocks.auroral.config.AuroralConfig;
import com.breakinblocks.auroral.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

import java.util.List;

@EventBusSubscriber(modid = Auroral.MOD_ID)
public class HearthwoodLogEventHandler {

    private static final double VILLAGER_DISCOUNT_RADIUS = 16.0;

    @SubscribeEvent
    public static void onPhantomChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Phantom)) {
            return;
        }
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget instanceof Player player && player.hasEffect(ModEffects.FROSTBITE_IMMUNITY)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effectToApply = event.getEffectInstance();

        // Check if trying to apply Frostbite - use .is() for proper Holder comparison
        if (effectToApply.getEffect().is(ModEffects.FROSTBITE.unwrapKey().orElseThrow())) {
            // Check if entity has Frostbite Immunity
            if (entity.hasEffect(ModEffects.FROSTBITE_IMMUNITY)) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            }
        }
    }

    @SubscribeEvent
    public static void onTradeScreenOpen(PlayerContainerEvent.Open event) {
        if (!(event.getContainer() instanceof MerchantMenu menu) || event.getEntity().level().isClientSide()) {
            return;
        }

        double discount = AuroralConfig.SERVER.hearthwoodLogVillagerDiscount.get();
        if (discount <= 0) {
            return;
        }

        Player player = event.getEntity();
        List<Villager> villagers = player.level().getEntitiesOfClass(Villager.class,
            player.getBoundingBox().inflate(8.0), villager -> villager.getTradingPlayer() == player);
        if (villagers.isEmpty() || !villagerHasHearthwoodLogDiscount(villagers.get(0))) {
            return;
        }

        for (MerchantOffer offer : menu.getOffers()) {
            int reduction = Mth.floor(offer.getBaseCostA().getCount() * discount);
            if (reduction > 0) {
                offer.addToSpecialPriceDiff(-reduction);
            }
        }
    }

    public static boolean isNearLitHearthwoodLog(Level level, BlockPos center, double radius) {
        return countNearbyLitHearthwoodLogs(level, center, radius) > 0;
    }

    public static int countNearbyLitHearthwoodLogs(Level level, BlockPos center, double radius) {
        int count = 0;
        int radiusInt = Mth.ceil(radius);
        double radiusSq = radius * radius;
        int minChunkX = (center.getX() - radiusInt) >> 4;
        int maxChunkX = (center.getX() + radiusInt) >> 4;
        int minChunkZ = (center.getZ() - radiusInt) >> 4;
        int maxChunkZ = (center.getZ() + radiusInt) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof HearthwoodLogBlockEntity log && log.isLit()
                        && blockEntity.getBlockPos().distSqr(center) <= radiusSq) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public static boolean villagerHasHearthwoodLogDiscount(Villager villager) {
        return isNearLitHearthwoodLog(villager.level(), villager.blockPosition(), VILLAGER_DISCOUNT_RADIUS);
    }
}
