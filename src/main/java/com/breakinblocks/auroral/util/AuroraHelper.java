package com.breakinblocks.auroral.util;

import com.breakinblocks.auroral.registry.ModDataAttachments;
import com.breakinblocks.auroral.registry.ModDataAttachments.AuroraState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Utility class for aurora-related queries and operations.
 */
public class AuroraHelper {

    /**
     * Checks if an aurora is currently active in the given level.
     *
     * @param level The level to check
     * @return true if aurora is active
     */
    public static boolean isAuroraActive(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            AuroraState state = serverLevel.getData(ModDataAttachments.AURORA_STATE);
            return state.active();
        }
        // For client-side, we'll need to sync this state via packets
        // For now, return false on client - will be updated when networking is added
        return false;
    }

    /**
     * Gets the aurora state for a server level.
     *
     * @param level The server level
     * @return The aurora state, or INACTIVE if not a server level
     */
    public static AuroraState getAuroraState(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getData(ModDataAttachments.AURORA_STATE);
        }
        return AuroraState.INACTIVE;
    }

    /**
     * Sets the aurora state for a server level.
     *
     * @param level The server level
     * @param state The new aurora state
     */
    public static void setAuroraState(ServerLevel level, AuroraState state) {
        level.setData(ModDataAttachments.AURORA_STATE, state);
    }

    /**
     * Starts an aurora event in the given level.
     *
     * @param level The server level
     * @param duration Duration in ticks
     */
    public static void startAurora(ServerLevel level, long duration) {
        long gameTime = level.getGameTime();
        AuroraState newState = new AuroraState(true, gameTime, gameTime + duration);
        setAuroraState(level, newState);
    }

    /**
     * Ends the current aurora event in the given level.
     *
     * @param level The server level
     */
    public static void endAurora(ServerLevel level) {
        setAuroraState(level, AuroraState.INACTIVE);
    }

    /**
     * Checks if a player at the given position is experiencing an active aurora.
     * This means aurora is active AND player is in a cold biome.
     *
     * @param level The level
     * @param pos The position (usually player position)
     * @return true if aurora effects should apply
     */
    public static boolean isExperiencingAurora(Level level, BlockPos pos) {
        return isAuroraActive(level) && BiomeHelper.canExperienceAurora(level, pos);
    }

    /**
     * Checks if it's currently night time in the level.
     * Night is from 13000 to 23000 day time.
     *
     * @param level The level
     * @return true if it's night
     */
    public static boolean isNightTime(Level level) {
        long dayTime = level.getDayTime() % 24000;
        return dayTime >= 13000 && dayTime < 23000;
    }

    /**
     * Checks if the day just transitioned to night.
     * This is used to trigger the aurora roll.
     *
     * @param level The level
     * @return true if this is the first tick of night
     */
    public static boolean isNightStart(Level level) {
        long dayTime = level.getDayTime() % 24000;
        // Check if we're at exactly 13000 (start of night)
        return dayTime == 13000;
    }
}
