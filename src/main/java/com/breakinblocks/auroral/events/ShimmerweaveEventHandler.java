package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.item.ShimmerweaveGogglesItem;
import com.breakinblocks.auroral.item.ShimmerweaveLeggingsItem;
import com.breakinblocks.auroral.item.ShimmerweaveSkatesItem;
import com.breakinblocks.auroral.item.ShimmerweaveTunicItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
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

/**
 * Event handler for Shimmerweave armor special abilities.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID)
public class ShimmerweaveEventHandler {

    /**
     * Attribute modifier ID for Shimmerweave Leggings snow speed boost.
     * Uses movement speed attribute so it stacks with Speed effect.
     */
    private static final Identifier LEGGINGS_SNOW_SPEED_ID = Auroral.id("shimmerweave_leggings_snow_speed");

    /**
     * Attribute modifier ID for Shimmerweave Leggings soul speed boost.
     */
    private static final Identifier LEGGINGS_SOUL_SPEED_ID = Auroral.id("shimmerweave_leggings_soul_speed");

    /**
     * Attribute modifier ID for Shimmerweave Skates ice/snow speed boost.
     */
    private static final Identifier SKATES_SPEED_ID = Auroral.id("shimmerweave_skates_speed");

    /**
     * Attribute modifier ID for Shimmerweave Skates packed ice speed boost.
     * This is a separate, larger boost specifically for packed ice and blue ice.
     */
    private static final Identifier SKATES_PACKED_ICE_SPEED_ID = Auroral.id("shimmerweave_skates_packed_ice_speed");

    /**
     * Speed boost multiplier for snow (20% faster).
     */
    private static final double SNOW_SPEED_BOOST = 0.2;

    /**
     * Speed boost multiplier for soul sand (30% faster, similar to Soul Speed III).
     */
    private static final double SOUL_SPEED_BOOST = 0.3;

    /**
     * Speed boost multiplier for ice/snow with skates (25% faster).
     */
    private static final double SKATES_SPEED_BOOST = 0.25;

    /**
     * Speed boost multiplier for packed/blue ice with skates (50% faster).
     * Packed ice is smoother and denser, allowing for faster skating.
     */
    private static final double SKATES_PACKED_ICE_SPEED_BOOST = 0.5;

    /**
     * Handles all Shimmerweave armor effects on player tick.
     */
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

    /**
     * Shimmerweave Goggles: Apply Glowing to hostile mobs within radius.
     */
    private static void handleGoggles(Player player, ServerLevel level) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof ShimmerweaveGogglesItem)) {
            return;
        }

        // Only apply effect every second (20 ticks) to reduce performance impact
        if (player.tickCount % 20 != 0) {
            return;
        }

        double radius = ShimmerweaveGogglesItem.GLOWING_RADIUS;
        AABB searchBox = player.getBoundingBox().inflate(radius);

        // Find all hostile mobs in range
        List<LivingEntity> hostiles = level.getEntitiesOfClass(LivingEntity.class, searchBox, entity -> {
            if (entity == player) return false;
            return entity.getType().getCategory() == MobCategory.MONSTER;
        });

        // Apply glowing effect to each hostile
        try {
            ResourceKey<MobEffect> glowingKey = ResourceKey.create(
                Registries.MOB_EFFECT,
                Identifier.withDefaultNamespace("glowing")
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
            // Effect not found, skip
        }
    }

    /**
     * Shimmerweave Tunic: Auto-extinguish fire.
     */
    private static void handleTunic(Player player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chestplate.getItem() instanceof ShimmerweaveTunicItem)) {
            return;
        }

        // Extinguish fire if player is on fire
        if (player.isOnFire()) {
            player.clearFire();
            // Small damage to armor for balance
            chestplate.hurtAndBreak(1, player, EquipmentSlot.CHEST);
        }
    }

    /**
     * Shimmerweave Leggings: Speed boost on snow, Soul Speed on soul sand.
     * Uses attribute modifiers so it stacks with Speed potions.
     */
    private static void handleLeggings(Player player) {
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        boolean hasLeggings = leggings.getItem() instanceof ShimmerweaveLeggingsItem;

        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        BlockPos belowPos = player.blockPosition().below();
        BlockState belowState = player.level().getBlockState(belowPos);

        // Check if on snow
        boolean onSnow = hasLeggings && (belowState.is(BlockTags.SNOW) ||
            belowState.is(Blocks.SNOW_BLOCK) || belowState.is(Blocks.POWDER_SNOW));

        // Check if on soul sand/soil
        boolean onSoul = hasLeggings && (belowState.is(Blocks.SOUL_SAND) || belowState.is(Blocks.SOUL_SOIL));

        // Handle snow speed modifier
        AttributeModifier snowModifier = speedAttribute.getModifier(LEGGINGS_SNOW_SPEED_ID);
        if (onSnow && snowModifier == null) {
            speedAttribute.addTransientModifier(new AttributeModifier(
                LEGGINGS_SNOW_SPEED_ID,
                SNOW_SPEED_BOOST,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        } else if (!onSnow && snowModifier != null) {
            speedAttribute.removeModifier(LEGGINGS_SNOW_SPEED_ID);
        }

        // Handle soul speed modifier
        AttributeModifier soulModifier = speedAttribute.getModifier(LEGGINGS_SOUL_SPEED_ID);
        if (onSoul && soulModifier == null) {
            speedAttribute.addTransientModifier(new AttributeModifier(
                LEGGINGS_SOUL_SPEED_ID,
                SOUL_SPEED_BOOST,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        } else if (!onSoul && soulModifier != null) {
            speedAttribute.removeModifier(LEGGINGS_SOUL_SPEED_ID);
        }

        // Remove modifiers if not wearing leggings
        if (!hasLeggings) {
            speedAttribute.removeModifier(LEGGINGS_SNOW_SPEED_ID);
            speedAttribute.removeModifier(LEGGINGS_SOUL_SPEED_ID);
        }
    }

    /**
     * Shimmerweave Skates: Speed boost on ice/snow, extra boost on packed ice, Frost Walker, Lava to Obsidian.
     * Uses attribute modifiers so it stacks with Speed potions.
     */
    private static void handleSkates(Player player, ServerLevel level) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        boolean hasSkates = boots.getItem() instanceof ShimmerweaveSkatesItem;

        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        BlockPos playerPos = player.blockPosition();
        BlockPos belowPos = playerPos.below();
        BlockState belowState = level.getBlockState(belowPos);

        // Check if on packed ice or blue ice (premium skating surface)
        boolean onPackedIce = hasSkates && (belowState.is(Blocks.PACKED_ICE) || belowState.is(Blocks.BLUE_ICE));

        // Check if on regular ice or snow (but not packed ice)
        boolean onIceOrSnow = hasSkates && !onPackedIce && (belowState.is(BlockTags.ICE) || belowState.is(BlockTags.SNOW) ||
            belowState.is(Blocks.SNOW_BLOCK) || belowState.is(Blocks.POWDER_SNOW));

        // Handle packed ice speed modifier (higher boost)
        AttributeModifier packedIceModifier = speedAttribute.getModifier(SKATES_PACKED_ICE_SPEED_ID);
        if (onPackedIce && packedIceModifier == null) {
            speedAttribute.addTransientModifier(new AttributeModifier(
                SKATES_PACKED_ICE_SPEED_ID,
                SKATES_PACKED_ICE_SPEED_BOOST,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        } else if (!onPackedIce && packedIceModifier != null) {
            speedAttribute.removeModifier(SKATES_PACKED_ICE_SPEED_ID);
        }

        // Handle regular skates speed modifier
        AttributeModifier skatesModifier = speedAttribute.getModifier(SKATES_SPEED_ID);
        if (onIceOrSnow && skatesModifier == null) {
            speedAttribute.addTransientModifier(new AttributeModifier(
                SKATES_SPEED_ID,
                SKATES_SPEED_BOOST,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        } else if (!onIceOrSnow && skatesModifier != null) {
            speedAttribute.removeModifier(SKATES_SPEED_ID);
        }

        // Remove modifiers if not wearing skates
        if (!hasSkates) {
            speedAttribute.removeModifier(SKATES_SPEED_ID);
            speedAttribute.removeModifier(SKATES_PACKED_ICE_SPEED_ID);
            return;
        }

        // Only process frost walker effect when player is moving and on ground
        if (!player.onGround() || (player.getDeltaMovement().x == 0 && player.getDeltaMovement().z == 0)) {
            return;
        }

        // Frost Walker + Lava Walker effect
        int radius = ShimmerweaveSkatesItem.FROST_WALKER_RADIUS;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                mutablePos.set(playerPos.getX() + x, playerPos.getY() - 1, playerPos.getZ() + z);

                if (mutablePos.closerThan(playerPos, radius + 0.5)) {
                    BlockState state = level.getBlockState(mutablePos);

                    // Freeze water to frosted ice
                    if (state.getBlock() == Blocks.WATER && state.getValue(LiquidBlock.LEVEL) == 0) {
                        BlockState aboveState = level.getBlockState(mutablePos.above());
                        if (aboveState.isAir()) {
                            level.setBlockAndUpdate(mutablePos, Blocks.FROSTED_ICE.defaultBlockState());
                        }
                    }

                    // Turn lava to obsidian (extended Frost Walker)
                    if (state.getFluidState().is(Fluids.LAVA) && state.getFluidState().isSource()) {
                        level.setBlockAndUpdate(mutablePos, Blocks.OBSIDIAN.defaultBlockState());
                        // Damage boots for turning lava
                        boots.hurtAndBreak(2, player, EquipmentSlot.FEET);
                    }
                }
            }
        }
    }

    /**
     * Shimmerweave Skates: Fall damage immunity on ice or obsidian.
     */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!(boots.getItem() instanceof ShimmerweaveSkatesItem)) {
            return;
        }

        // Check if landing on ice or obsidian
        BlockPos landingPos = player.blockPosition().below();
        BlockState landingState = player.level().getBlockState(landingPos);

        if (landingState.is(BlockTags.ICE) || landingState.is(Blocks.OBSIDIAN) ||
            landingState.is(Blocks.CRYING_OBSIDIAN)) {
            event.setCanceled(true);
        }
    }

}
