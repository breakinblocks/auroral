package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.effect.FrostbiteEffect;
import com.breakinblocks.auroral.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

/**
 * Event handler for Frostbite effect mechanics.
 * Handles healing reduction.
 * Armor reduction is handled via attribute modifiers in the effect itself.
 *
 * Note: Shield brittleness would require a ShieldBlockEvent which is not available
 * in NeoForge 1.21.11. This can be implemented via mixin or alternative approach later.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID)
public class FrostbiteEventHandler {

    /**
     * Reduces healing when entity has Frostbite effect.
     * Healing is reduced by 25% per level.
     */
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance frostbite = entity.getEffect(ModEffects.FROSTBITE);

        if (frostbite != null) {
            float multiplier = FrostbiteEffect.getHealingMultiplier(frostbite.getAmplifier());
            float newHealAmount = event.getAmount() * multiplier;

            if (newHealAmount <= 0) {
                event.setCanceled(true);
            } else {
                event.setAmount(newHealAmount);
            }
        }
    }
}
