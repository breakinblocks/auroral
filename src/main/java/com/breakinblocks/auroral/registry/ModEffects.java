package com.breakinblocks.auroral.registry;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.effect.FrostbiteEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for all Auroral mob effects.
 */
public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
        DeferredRegister.create(Registries.MOB_EFFECT, Auroral.MOD_ID);

    /**
     * Frostbite - A debilitating cold effect that:
     * - Reduces healing effectiveness
     * - Makes shields brittle (reduced blocking)
     * - Temporarily lowers armor effectiveness
     */
    public static final Holder<MobEffect> FROSTBITE = MOB_EFFECTS.register("frostbite",
        () -> new FrostbiteEffect(MobEffectCategory.HARMFUL, 0x7DD3FC) // Light ice blue color
    );

    /**
     * Frostbite Immunity - Beneficial effect granted by Hearthwood Log.
     * Prevents Frostbite from being applied while active.
     */
    public static final Holder<MobEffect> FROSTBITE_IMMUNITY = MOB_EFFECTS.register("frostbite_immunity",
        () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFFD700) {} // Gold color
    );
}
