package com.breakinblocks.auroral.registry;

import com.breakinblocks.auroral.Auroral;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for custom sounds.
 *
 * PLACEHOLDER: Replace the sound files in assets/auroral/sounds/ with actual sound files.
 * Sound files should be .ogg format.
 */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(Registries.SOUND_EVENT, Auroral.MOD_ID);

    // Aurora ambient sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> AURORA_AMBIENT = registerSound("aurora_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> AURORA_START = registerSound("aurora_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> AURORA_END = registerSound("aurora_end");

    // Aurora music (for weather channel)
    public static final DeferredHolder<SoundEvent, SoundEvent> AURORA_MUSIC = registerSound("aurora_music");

    // Basin infusion sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> BASIN_INFUSE = registerSound("basin_infuse");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASIN_FILL = registerSound("basin_fill");

    // Star-Shot sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_SHOT_FIRE = registerSound("star_shot_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_SHOT_IMPACT = registerSound("star_shot_impact");

    // Auroral Nautilus sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> NAUTILUS_AMBIENT = registerSound("nautilus_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> NAUTILUS_SPAWN = registerSound("nautilus_spawn");
    public static final DeferredHolder<SoundEvent, SoundEvent> NAUTILUS_DEATH = registerSound("nautilus_death");

    // Hearthwood Log sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTHWOOD_LOG_CRACKLE = registerSound("hearthwood_log_crackle");
    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTHWOOD_LOG_IGNITE = registerSound("hearthwood_log_ignite");

    // Shimmerweave armor sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> SKATES_SLIDE = registerSound("skates_slide");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Auroral.id(name)));
    }
}
