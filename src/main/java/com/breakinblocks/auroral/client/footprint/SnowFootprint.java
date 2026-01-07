package com.breakinblocks.auroral.client.footprint;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Represents a single footprint on snow.
 */
public class SnowFootprint {
    /** World position of the footprint */
    public final Vec3 position;
    /** Block position (for culling) */
    public final BlockPos blockPos;
    /** Y rotation of the footprint in radians */
    public final float rotation;
    /** Whether this is a left foot (false = right foot) */
    public final boolean isLeftFoot;
    /** Game time when the footprint was created */
    public final long creationTime;
    /** Total lifetime in ticks */
    public final int lifetime;

    public SnowFootprint(Vec3 position, float rotation, boolean isLeftFoot, long creationTime, int lifetime) {
        this.position = position;
        this.blockPos = BlockPos.containing(position);
        this.rotation = rotation;
        this.isLeftFoot = isLeftFoot;
        this.creationTime = creationTime;
        this.lifetime = lifetime;
    }

    /**
     * Calculates the age of this footprint in ticks.
     */
    public long getAge(long currentTime) {
        return currentTime - creationTime;
    }

    /**
     * Checks if this footprint has expired.
     */
    public boolean isExpired(long currentTime) {
        return getAge(currentTime) >= lifetime;
    }

    /**
     * Gets the opacity multiplier for rendering (1.0 = fully visible, 0.0 = invisible).
     * Footprints fade out over their lifetime.
     */
    public float getOpacity(long currentTime) {
        long age = getAge(currentTime);
        if (age >= lifetime) {
            return 0.0f;
        }
        // Start fading at 50% of lifetime
        float fadeStart = lifetime * 0.5f;
        if (age < fadeStart) {
            return 1.0f;
        }
        return 1.0f - ((age - fadeStart) / (lifetime - fadeStart));
    }
}
