package com.breakinblocks.auroral.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class AuroralConfig {
    public static final ModConfigSpec clientSpec;
    public static final ClientConfig CLIENT;

    public static final ModConfigSpec serverSpec;
    public static final ServerConfig SERVER;

    public static final ModConfigSpec startupSpec;
    public static final StartupConfig STARTUP;

    static {
        final Pair<ClientConfig, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(ClientConfig::new);
        clientSpec = clientPair.getRight();
        CLIENT = clientPair.getLeft();

        final Pair<ServerConfig, ModConfigSpec> serverPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        serverSpec = serverPair.getRight();
        SERVER = serverPair.getLeft();

        final Pair<StartupConfig, ModConfigSpec> startupPair = new ModConfigSpec.Builder().configure(StartupConfig::new);
        startupSpec = startupPair.getRight();
        STARTUP = startupPair.getLeft();
    }

    public static class ClientConfig {
        public final ModConfigSpec.BooleanValue showAuroraEffect;
        public final ModConfigSpec.BooleanValue showAuroraParticles;
        public final ModConfigSpec.DoubleValue auroraIntensity;
        public final ModConfigSpec.BooleanValue playAuroraAmbientSound;
        public final ModConfigSpec.BooleanValue showFootprints;

        ClientConfig(ModConfigSpec.Builder builder) {
            builder.comment("Auroral Client Configuration")
                   .push("aurora_visuals");

            showAuroraEffect = builder
                .comment("Show the aurora borealis sky effect")
                .define("show_aurora_effect", true);

            showAuroraParticles = builder
                .comment("Show particle effects during aurora events")
                .define("show_aurora_particles", true);

            auroraIntensity = builder
                .comment("Visual intensity of the aurora effect (0.0 to 2.0)")
                .defineInRange("aurora_intensity", 1.0, 0.0, 2.0);

            playAuroraAmbientSound = builder
                .comment("Play ambient sounds during aurora events")
                .define("play_aurora_ambient_sound", true);

            builder.pop().push("footprints");

            showFootprints = builder
                .comment("Show snow footprints when walking on snow")
                .define("show_footprints", true);

            builder.pop();
        }
    }

    public static class ServerConfig {
        // Aurora settings
        public final ModConfigSpec.DoubleValue auroraChance;
        public final ModConfigSpec.IntValue auroraMinDuration;
        public final ModConfigSpec.IntValue auroraMaxDuration;
        public final ModConfigSpec.IntValue auroraRepairRate;

        // Combat settings
        public final ModConfigSpec.DoubleValue executeThreshold;
        public final ModConfigSpec.DoubleValue swordSlownessDuration;

        // Equipment settings
        public final ModConfigSpec.IntValue gogglesGlowingRadius;
        public final ModConfigSpec.IntValue skatesSpeedAmplifier;

        // Hearthwood Log settings
        public final ModConfigSpec.IntValue hearthwoodLogBurnTime;
        public final ModConfigSpec.IntValue hearthwoodLogFrostbiteRadius;
        public final ModConfigSpec.DoubleValue hearthwoodLogVillagerDiscount;

        // Basin settings
        public final ModConfigSpec.IntValue basinFillRate;
        public final ModConfigSpec.IntValue basinMaxAura;

        ServerConfig(ModConfigSpec.Builder builder) {
            builder.comment("Auroral Server Configuration")
                   .push("aurora");

            auroraChance = builder
                .comment("Chance of an aurora occurring each night in cold biomes (0.0 to 1.0)")
                .defineInRange("aurora_chance", 0.33, 0.0, 1.0);

            auroraMinDuration = builder
                .comment("Minimum duration of an aurora in ticks (20 ticks = 1 second)")
                .defineInRange("aurora_min_duration", 6000, 1000, 24000);

            auroraMaxDuration = builder
                .comment("Maximum duration of an aurora in ticks")
                .defineInRange("aurora_max_duration", 12000, 1000, 24000);

            auroraRepairRate = builder
                .comment("Durability repaired per second on Shimmersteel/Shimmerweave gear during aurora")
                .defineInRange("aurora_repair_rate", 1, 0, 10);

            builder.pop().push("combat");

            executeThreshold = builder
                .comment("Health percentage threshold for Shimmersteel Sword execute (0.0 to 1.0)")
                .defineInRange("execute_threshold", 0.15, 0.0, 0.5);

            swordSlownessDuration = builder
                .comment("Duration in seconds of Slowness applied by Shimmersteel Sword")
                .defineInRange("sword_slowness_duration", 3.0, 0.5, 10.0);

            builder.pop().push("equipment");

            gogglesGlowingRadius = builder
                .comment("Radius in blocks for Shimmersteel Goggles to apply Glowing effect")
                .defineInRange("goggles_glowing_radius", 32, 8, 64);

            skatesSpeedAmplifier = builder
                .comment("Speed effect amplifier for Shimmerweave Skates on ice/snow (0 = Speed I)")
                .defineInRange("skates_speed_amplifier", 0, 0, 3);

            builder.pop().push("hearthwood_log");

            hearthwoodLogBurnTime = builder
                .comment("How long the Hearthwood Log burns in ticks (168000 = 7 in-game days)")
                .defineInRange("hearthwood_log_burn_time", 168000, 24000, 672000);

            hearthwoodLogFrostbiteRadius = builder
                .comment("Radius for Hearthwood Log frostbite protection")
                .defineInRange("hearthwood_log_frostbite_radius", 16, 4, 32);

            hearthwoodLogVillagerDiscount = builder
                .comment("Trade discount percentage for villagers near Hearthwood Log (0.0 to 1.0)")
                .defineInRange("hearthwood_log_villager_discount", 0.2, 0.0, 0.5);

            builder.pop().push("basin");

            basinFillRate = builder
                .comment("Ticks between each aura fill during aurora (lower = faster)")
                .defineInRange("basin_fill_rate", 100, 20, 1200);

            basinMaxAura = builder
                .comment("Maximum aura levels a basin can hold")
                .defineInRange("basin_max_aura", 3, 1, 10);

            builder.pop();
        }
    }

    /**
     * Startup config - loaded before server startup.
     * Used for settings that affect item registration/components.
     */
    public static class StartupConfig {
        public final ModConfigSpec.IntValue snowballMaxStackSize;

        StartupConfig(ModConfigSpec.Builder builder) {
            builder.comment("Auroral Startup Configuration",
                           "These settings are loaded early and require a game restart to change.")
                   .push("items");

            snowballMaxStackSize = builder
                .comment("Maximum stack size for snowballs (vanilla default is 16)")
                .defineInRange("snowball_max_stack_size", 64, 16, 64);

            builder.pop();
        }
    }
}
