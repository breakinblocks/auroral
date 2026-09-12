package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.item.ShimmerSpearItem;
import com.breakinblocks.auroral.item.ShimmersteelHoeItem;
import com.breakinblocks.auroral.item.ShimmersteelPickaxeItem;
import com.breakinblocks.auroral.item.ShimmersteelShovelItem;
import com.breakinblocks.auroral.item.ShimmersteelSwordItem;
import com.breakinblocks.auroral.registry.ModEffects;
import com.breakinblocks.auroral.util.AuroraHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Event handler for Shimmersteel tool special abilities.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID)
public class ShimmersteelEventHandler {

    private static final int SPEAR_FROSTBITE_DURATION_TICKS = 100;
    private static final int SPEAR_AURORA_FROSTBITE_DURATION_TICKS = 200;
    private static final float SPEAR_AURORA_DAMAGE_BONUS = 2.0F;

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();

        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        LivingEntity target = event.getEntity();
        if (target == attacker) {
            return;
        }

        ItemStack heldWeapon = source.getWeaponItem();
        if (heldWeapon == null) {
            heldWeapon = attacker.getMainHandItem();
        }

        if (heldWeapon.getItem() instanceof ShimmerSpearItem) {
            applyShimmerSpearEffects(event, attacker, target);
            return;
        }

        ItemStack weapon = source.getWeaponItem();
        if (weapon == null || !(weapon.getItem() instanceof ShimmersteelSwordItem)) {
            weapon = attacker.getMainHandItem();
        }
        if (!(weapon.getItem() instanceof ShimmersteelSwordItem)) {
            return;
        }

        float currentHealth = target.getHealth();
        float damageAmount = event.getNewDamage();
        float healthAfterDamage = currentHealth - damageAmount;
        float maxHealth = target.getMaxHealth();
        float executeThreshold = com.breakinblocks.auroral.config.AuroralConfig.SERVER.executeThreshold.get().floatValue();
        float thresholdHealth = maxHealth * executeThreshold;

        boolean alreadyBelowThreshold = currentHealth <= thresholdHealth && currentHealth > 0;
        boolean wouldDropBelowThreshold = healthAfterDamage <= thresholdHealth && currentHealth > 0;

        if (alreadyBelowThreshold || wouldDropBelowThreshold) {
            event.setNewDamage(Float.MAX_VALUE);

            if (target.level() instanceof ServerLevel serverLevel) {
                double x = target.getX();
                double y = target.getY() + target.getBbHeight() * 0.5;
                double z = target.getZ();

                serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, 30, 0.5, 0.8, 0.5, 0.5);
                serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, 20, 0.4, 0.6, 0.4, 0.2);
                serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 8, 0.3, 0.5, 0.3, 0.05);

                serverLevel.playSound(null, x, y, z,
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.6F);
                serverLevel.playSound(null, x, y, z,
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.5F);

                BlockPos deathPos = target.blockPosition();
                MinecraftServer server = serverLevel.getServer();
                server.schedule(new TickTask(server.getTickCount() + 1, () -> {
                    if (target.isDeadOrDying()) {
                        ShimmersteelSwordItem.placeSnowOnKill(serverLevel, deathPos);
                    }
                }));
            }
        }
    }

    private static void applyShimmerSpearEffects(LivingDamageEvent.Pre event, LivingEntity attacker, LivingEntity target) {
        boolean auroraActive = AuroraHelper.isAuroraActive(attacker.level());

        if (auroraActive) {
            event.setNewDamage(event.getNewDamage() + SPEAR_AURORA_DAMAGE_BONUS);
        }

        int duration = auroraActive ? SPEAR_AURORA_FROSTBITE_DURATION_TICKS : SPEAR_FROSTBITE_DURATION_TICKS;
        int amplifier = auroraActive ? 1 : 0;
        target.addEffect(new MobEffectInstance(ModEffects.FROSTBITE, duration, amplifier));

        if (target.level() instanceof ServerLevel serverLevel) {
            double x = target.getX();
            double y = target.getY() + target.getBbHeight() * 0.5;
            double z = target.getZ();
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 8, 0.3, 0.3, 0.3, 0.05);
            if (auroraActive) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 4, 0.2, 0.2, 0.2, 0.03);
            }
        }
    }

    /**
     * Handles Fortune III for Shimmersteel Pickaxe on gem blocks
     * and Silk Touch for Shimmersteel Shovel.
     */
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        Entity breaker = event.getBreaker();
        if (breaker == null) {
            return;
        }

        ItemStack tool = event.getTool();
        BlockState state = event.getState();
        ServerLevel level = event.getLevel();
        BlockPos pos = event.getPos();

        // Shimmersteel Pickaxe: Fortune III for gems
        if (tool.getItem() instanceof ShimmersteelPickaxeItem) {
            if (ShimmersteelPickaxeItem.isGemBlock(state)) {
                applyFortuneBonus(event.getDrops(), level.getRandom(), 3);
            }
        }

        // Shimmersteel Shovel: Silk Touch
        if (tool.getItem() instanceof ShimmersteelShovelItem) {
            applySilkTouch(event, state, pos, tool);
        }

        // Shimmersteel Hoe: Silk Touch (for crops and other blocks)
        if (tool.getItem() instanceof ShimmersteelHoeItem) {
            applySilkTouch(event, state, pos, tool);
        }
    }

    /**
     * Applies Fortune bonus to drops by multiplying item counts.
     * Uses vanilla Fortune formula: bonus = random(0 to fortuneLevel) + 1
     */
    private static void applyFortuneBonus(List<ItemEntity> drops, RandomSource random, int fortuneLevel) {
        for (ItemEntity itemEntity : drops) {
            ItemStack stack = itemEntity.getItem();
            if (stack.getMaxStackSize() > 1) {
                // Fortune formula: multiplier is 1 + random(0 to fortuneLevel)
                // So Fortune III gives 1-4x drops
                int multiplier = 1 + random.nextInt(fortuneLevel + 1);
                if (multiplier > 1) {
                    int newCount = Math.min(stack.getCount() * multiplier, stack.getMaxStackSize());
                    stack.setCount(newCount);
                }
            }
        }
    }

    /**
     * Replaces normal drops with silk touch drops (the block itself).
     */
    private static void applySilkTouch(BlockDropsEvent event, BlockState state, BlockPos pos, ItemStack tool) {
        ServerLevel level = event.getLevel();
        Holder<Enchantment> silkTouch = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        if (tool.getEnchantmentLevel(silkTouch) > 0) {
            return;
        }

        ItemStack silkTool = tool.copy();
        silkTool.enchant(silkTouch, 1);

        List<ItemEntity> drops = event.getDrops();
        drops.clear();

        for (ItemStack drop : Block.getDrops(state, level, pos, event.getBlockEntity(), event.getBreaker(), silkTool)) {
            ItemEntity newDrop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
            newDrop.setDefaultPickUpDelay();
            drops.add(newDrop);
        }
    }
}
