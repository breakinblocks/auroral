package com.breakinblocks.auroral.client;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModParticles;
import com.breakinblocks.auroral.util.BiomeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Client-side handler for Aurora Weather ambient effects.
 *
 * When an aurora is active, spawns ambient particles around the player
 * to create an immersive "weather" experience. Particles gently fall from
 * the sky like snowflakes imbued with aurora light.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID, value = Dist.CLIENT)
public class AuroraWeatherHandler {

    /**
     * Interval between particle spawns (every 4 ticks).
     */
    private static final int SPAWN_INTERVAL = 4;

    /**
     * Maximum horizontal distance from player for particle spawns.
     */
    private static final int HORIZONTAL_RADIUS = 24;

    /**
     * How high above the player particles can spawn.
     */
    private static final int HEIGHT_ABOVE = 15;

    /**
     * Number of particles to spawn per interval.
     */
    private static final int PARTICLES_PER_TICK = 3;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // Only process client player
        if (!(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }

        Level level = player.level();

        // Only spawn particles when aurora is active
        if (!ClientAuroraState.isAuroraActive()) {
            return;
        }

        // Only spawn in cold biomes
        if (!BiomeHelper.isColdBiome(level, player.blockPosition())) {
            return;
        }

        // Only spawn at intervals
        if (player.tickCount % SPAWN_INTERVAL != 0) {
            return;
        }

        // Must be outside with sky access to see aurora particles
        BlockPos playerPos = player.blockPosition();
        if (!level.canSeeSky(playerPos)) {
            return;
        }

        RandomSource random = level.getRandom();

        // Spawn several aurora particles falling from the sky
        for (int i = 0; i < PARTICLES_PER_TICK; i++) {
            double offsetX = (random.nextDouble() - 0.5) * HORIZONTAL_RADIUS * 2;
            double offsetY = random.nextDouble() * HEIGHT_ABOVE + 5;
            double offsetZ = (random.nextDouble() - 0.5) * HORIZONTAL_RADIUS * 2;

            double x = player.getX() + offsetX;
            double y = player.getY() + offsetY;
            double z = player.getZ() + offsetZ;

            // Slow downward velocity with slight drift
            double vx = (random.nextDouble() - 0.5) * 0.02;
            double vy = -0.02 - random.nextDouble() * 0.02;
            double vz = (random.nextDouble() - 0.5) * 0.02;

            // Alternate between aurora sparkle and shimmer particles
            if (random.nextBoolean()) {
                level.addParticle(ModParticles.AURORA_SPARKLE.get(), x, y, z, vx, vy, vz);
            } else {
                level.addParticle(ModParticles.SHIMMER.get(), x, y, z, vx, vy, vz);
            }
        }

        // Occasionally spawn a cluster near ground level for extra ambiance
        if (random.nextFloat() < 0.1f) {
            double groundX = player.getX() + (random.nextDouble() - 0.5) * 16;
            double groundZ = player.getZ() + (random.nextDouble() - 0.5) * 16;
            double groundY = player.getY() + random.nextDouble() * 3;

            for (int i = 0; i < 3; i++) {
                double clusterX = groundX + (random.nextDouble() - 0.5) * 2;
                double clusterY = groundY + random.nextDouble() * 0.5;
                double clusterZ = groundZ + (random.nextDouble() - 0.5) * 2;

                level.addParticle(ModParticles.SHIMMER.get(),
                    clusterX, clusterY, clusterZ,
                    0, 0.01, 0);
            }
        }
    }
}
