package com.breakinblocks.auroral.util;

import com.breakinblocks.auroral.registry.ModDataAttachments;
import com.breakinblocks.auroral.registry.ModDataAttachments.AuroraState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class AuroraHelper {

    public static boolean isAuroraActive(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            AuroraState state = serverLevel.getData(ModDataAttachments.AURORA_STATE);
            return state.active();
        }
        return false;
    }

    public static AuroraState getAuroraState(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getData(ModDataAttachments.AURORA_STATE);
        }
        return AuroraState.INACTIVE;
    }

    public static void setAuroraState(ServerLevel level, AuroraState state) {
        level.setData(ModDataAttachments.AURORA_STATE, state);
    }

    public static void startAurora(ServerLevel level, long duration) {
        long gameTime = level.getGameTime();
        AuroraState newState = new AuroraState(true, gameTime, gameTime + duration);
        setAuroraState(level, newState);
    }

    public static void endAurora(ServerLevel level) {
        setAuroraState(level, AuroraState.INACTIVE);
    }

    public static boolean isExperiencingAurora(Level level, BlockPos pos) {
        return isAuroraActive(level) && BiomeHelper.canExperienceAurora(level, pos);
    }

    public static long getDayTime(Level level) {
        long ticks = level.dimensionType().defaultClock().isPresent()
            ? level.getDefaultClockTime()
            : level.getOverworldClockTime();
        return ticks % 24000;
    }

    public static boolean isNightTime(Level level) {
        long dayTime = getDayTime(level);
        return dayTime >= 13000 && dayTime < 23000;
    }

    public static boolean isNightStart(Level level) {
        long dayTime = getDayTime(level);
        return dayTime == 13000;
    }
}
