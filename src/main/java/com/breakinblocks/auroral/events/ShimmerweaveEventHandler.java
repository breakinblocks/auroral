package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.config.AuroralConfig;
import com.breakinblocks.auroral.item.ShimmerweaveGogglesItem;
import com.breakinblocks.auroral.item.ShimmerweaveLeggingsItem;
import com.breakinblocks.auroral.item.ShimmerweaveSkatesItem;
import com.breakinblocks.auroral.item.ShimmerweaveTunicItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = Auroral.MOD_ID)
public class ShimmerweaveEventHandler {

    private static final ResourceLocation LEGGINGS_SNOW_SPEED_ID = Auroral.id("shimmerweave_leggings_snow_speed");
    private static final ResourceLocation LEGGINGS_SOUL_SPEED_ID = Auroral.id("shimmerweave_leggings_soul_speed");
    private static final ResourceLocation SKATES_ICE_SPEED_ID = Auroral.id("shimmerweave_skates_ice_speed");
    private static final ResourceLocation SKATES_PACKED_ICE_SPEED_ID = Auroral.id("shimmerweave_skates_packed_ice_speed");

    private static double getSnowSpeedBoost() {
        return AuroralConfig.SERVER.leggingsSnowSpeedBoost.get();
    }

    private static double getSoulSpeedBoost() {
        return AuroralConfig.SERVER.leggingsSoulSpeedBoost.get();
    }

    private static double getSkatesSpeedBoost() {
        return AuroralConfig.SERVER.skatesIceSpeedBoost.get();
    }

    private static double getSkatesPackedIceSpeedBoost() {
        return AuroralConfig.SERVER.skatesPackedIceSpeedBoost.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide()) {
            return;
        }

        // Check each armor piece and apply effects
        handleGoggles(player, (ServerLevel) level);
        handleTunic(player);
        handleLeggings(player);
        handleSkates(player, (ServerLevel) level);
    }

    private static void handleGoggles(Player player, ServerLevel level) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof ShimmerweaveGogglesItem)) {
            return;
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        double radius = AuroralConfig.SERVER.gogglesGlowingRadius.get();
        AABB searchBox = player.getBoundingBox().inflate(radius);

        List<LivingEntity> hostiles = level.getEntitiesOfClass(LivingEntity.class, searchBox, entity -> {
            if (entity == player) return false;
            return entity.getType().getCategory() == MobCategory.MONSTER;
        });

        try {
            ResourceKey<MobEffect> glowingKey = ResourceKey.create(
                Registries.MOB_EFFECT,
                ResourceLocation.withDefaultNamespace("glowing")
            );
            Holder<MobEffect> glowingHolder = level.registryAccess()
                .lookupOrThrow(Registries.MOB_EFFECT)
                .getOrThrow(glowingKey);

            for (LivingEntity hostile : hostiles) {
                hostile.addEffect(new MobEffectInstance(
                    glowingHolder,
                    ShimmerweaveGogglesItem.GLOWING_DURATION,
                    0,
                    false,
                    false,
                    true
                ));
            }
        } catch (Exception e) {
        }
    }

    private static void handleTunic(Player player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chestplate.getItem() instanceof ShimmerweaveTunicItem)) {
            return;
        }

        if (player.isOnFire()) {
            player.clearFire();
            chestplate.hurtAndBreak(1, player, EquipmentSlot.CHEST);
        }
    }

    private static void handleLeggings(Player player) {
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        boolean hasLeggings = leggings.getItem() instanceof ShimmerweaveLeggingsItem;

        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        BlockPos playerPos = player.blockPosition();
        BlockPos belowPos = playerPos.below();
        BlockState playerState = player.level().getBlockState(playerPos);
        BlockState belowState = player.level().getBlockState(belowPos);

        boolean onSnow = hasLeggings && (
            playerState.is(BlockTags.SNOW) || playerState.is(Blocks.POWDER_SNOW) ||
            belowState.is(BlockTags.SNOW) || belowState.is(Blocks.SNOW_BLOCK) || belowState.is(Blocks.POWDER_SNOW));

        boolean onSoul = hasLeggings && (belowState.is(Blocks.SOUL_SAND) || belowState.is(Blocks.SOUL_SOIL));

        AttributeModifier snowModifier = speedAttribute.getModifier(LEGGINGS_SNOW_SPEED_ID);
        if (onSnow && snowModifier == null) {
            speedAttribute.addTransientModifier(new AttributeModifier(
                LEGGINGS_SNOW_SPEED_ID,
                getSnowSpeedBoost(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        } else if (!onSnow && snowModifier != null) {
            speedAttribute.removeModifier(LEGGINGS_SNOW_SPEED_ID);
        }

        AttributeModifier soulModifier = speedAttribute.getModifier(LEGGINGS_SOUL_SPEED_ID);
        if (onSoul && soulModifier == null) {
            speedAttribute.addTransientModifier(new AttributeModifier(
                LEGGINGS_SOUL_SPEED_ID,
                getSoulSpeedBoost(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        } else if (!onSoul && soulModifier != null) {
            speedAttribute.removeModifier(LEGGINGS_SOUL_SPEED_ID);
        }

        if (!hasLeggings) {
            speedAttribute.removeModifier(LEGGINGS_SNOW_SPEED_ID);
            speedAttribute.removeModifier(LEGGINGS_SOUL_SPEED_ID);
        }
    }

    private static void handleSkates(Player player, ServerLevel level) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        boolean hasSkates = boots.getItem() instanceof ShimmerweaveSkatesItem;

        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        BlockPos playerPos = player.blockPosition();
        BlockPos belowPos = playerPos.below();
        BlockState belowState = level.getBlockState(belowPos);

        boolean onPackedIce = hasSkates && (belowState.is(Blocks.PACKED_ICE) || belowState.is(Blocks.BLUE_ICE));
        boolean onRegularIce = hasSkates && !onPackedIce && belowState.is(BlockTags.ICE);

        AttributeModifier packedIceModifier = speedAttribute.getModifier(SKATES_PACKED_ICE_SPEED_ID);
        if (onPackedIce && packedIceModifier == null) {
            speedAttribute.addTransientModifier(new AttributeModifier(
                SKATES_PACKED_ICE_SPEED_ID,
                getSkatesPackedIceSpeedBoost(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        } else if (!onPackedIce && packedIceModifier != null) {
            speedAttribute.removeModifier(SKATES_PACKED_ICE_SPEED_ID);
        }

        AttributeModifier iceModifier = speedAttribute.getModifier(SKATES_ICE_SPEED_ID);
        if (onRegularIce && iceModifier == null) {
            speedAttribute.addTransientModifier(new AttributeModifier(
                SKATES_ICE_SPEED_ID,
                getSkatesSpeedBoost(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        } else if (!onRegularIce && iceModifier != null) {
            speedAttribute.removeModifier(SKATES_ICE_SPEED_ID);
        }

        if (!hasSkates) {
            speedAttribute.removeModifier(SKATES_ICE_SPEED_ID);
            speedAttribute.removeModifier(SKATES_PACKED_ICE_SPEED_ID);
            return;
        }

        if (!player.onGround() || (player.getDeltaMovement().x == 0 && player.getDeltaMovement().z == 0)) {
            return;
        }

        int radius = AuroralConfig.SERVER.skatesFrostWalkerRadius.get();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                mutablePos.set(playerPos.getX() + x, playerPos.getY() - 1, playerPos.getZ() + z);

                if (mutablePos.closerThan(playerPos, radius + 0.5)) {
                    BlockState state = level.getBlockState(mutablePos);

                    if (state.getBlock() == Blocks.WATER && state.getValue(LiquidBlock.LEVEL) == 0) {
                        BlockState aboveState = level.getBlockState(mutablePos.above());
                        if (aboveState.isAir()) {
                            level.setBlockAndUpdate(mutablePos, Blocks.FROSTED_ICE.defaultBlockState());
                        }
                    }

                    if (state.getFluidState().is(Fluids.LAVA) && state.getFluidState().isSource()) {
                        level.setBlockAndUpdate(mutablePos, Blocks.OBSIDIAN.defaultBlockState());
                        boots.hurtAndBreak(2, player, EquipmentSlot.FEET);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!(boots.getItem() instanceof ShimmerweaveSkatesItem)) {
            return;
        }

        BlockPos landingPos = player.blockPosition().below();
        BlockState landingState = player.level().getBlockState(landingPos);

        if (landingState.is(BlockTags.ICE) || landingState.is(Blocks.OBSIDIAN) ||
            landingState.is(Blocks.CRYING_OBSIDIAN)) {
            event.setCanceled(true);
        }
    }

}
