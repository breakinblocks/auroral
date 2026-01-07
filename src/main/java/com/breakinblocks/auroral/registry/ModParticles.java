package com.breakinblocks.auroral.registry;

import com.breakinblocks.auroral.Auroral;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for custom particle types.
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
        DeferredRegister.create(Registries.PARTICLE_TYPE, Auroral.MOD_ID);

    // Aurora sparkle particle - used for aurora ambient effects
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AURORA_SPARKLE =
        PARTICLES.register("aurora_sparkle", () -> new SimpleParticleType(false));

    // Basin infusion particle - swirl effect during item transformation
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BASIN_INFUSE =
        PARTICLES.register("basin_infuse", () -> new SimpleParticleType(false));

    // Star shot trail particle - glowing trail behind star shots
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STAR_TRAIL =
        PARTICLES.register("star_trail", () -> new SimpleParticleType(false));

    // Frost particle - used for cold effects
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FROST =
        PARTICLES.register("frost", () -> new SimpleParticleType(false));

    // Shimmer particle - general shimmering effect
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHIMMER =
        PARTICLES.register("shimmer", () -> new SimpleParticleType(false));
}
