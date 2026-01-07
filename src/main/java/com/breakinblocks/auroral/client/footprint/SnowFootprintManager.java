package com.breakinblocks.auroral.client.footprint;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.config.AuroralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages the creation and lifecycle of snow footprints on the client.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID, value = Dist.CLIENT)
public class SnowFootprintManager {

    /** Maximum number of footprints to track */
    private static final int MAX_FOOTPRINTS = 200;

    /** How long footprints last in ticks (10 seconds) */
    private static final int FOOTPRINT_LIFETIME = 200;

    /** Minimum distance traveled before creating a new footprint */
    private static final double MIN_STEP_DISTANCE = 0.6;

    /** Offset from center for left/right foot */
    private static final double FOOT_OFFSET = 0.15;

    /** List of active footprints */
    private static final List<SnowFootprint> footprints = new ArrayList<>();

    /** Last position where a footprint was placed */
    private static Vec3 lastFootprintPos = null;

    /** Whether the next footprint should be left foot */
    private static boolean nextIsLeftFoot = true;

    /** Last player Y rotation for footprint direction */
    private static float lastYRot = 0;

    /**
     * Gets the list of active footprints for rendering.
     */
    public static List<SnowFootprint> getFootprints() {
        return footprints;
    }

    /**
     * Clears all footprints. Called on disconnect.
     */
    public static void clear() {
        footprints.clear();
        lastFootprintPos = null;
        nextIsLeftFoot = true;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // Check config first
        if (!AuroralConfig.CLIENT.showFootprints.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;

        if (player == null || level == null) {
            return;
        }

        // Don't create footprints if player is flying, swimming, or not on ground
        if (player.getAbilities().flying || player.isSwimming() || !player.onGround()) {
            return;
        }

        // Check if player is on snow
        // Check the block at player's feet position and below
        BlockPos feetPos = player.blockPosition();
        BlockPos belowPos = feetPos.below();

        // Also check two blocks below for cases where player is on top of snow layer
        BlockPos twoBelow = belowPos.below();
        if (!isSnowySurface(level, feetPos) && !isSnowySurface(level, belowPos) && !isSnowySurface(level, twoBelow)) {
            return;
        }

        Vec3 playerPos = player.position();
        long gameTime = level.getGameTime();

        // Check if we've moved enough to create a new footprint
        if (lastFootprintPos == null) {
            lastFootprintPos = playerPos;
            lastYRot = player.getYRot();
            return;
        }

        double distance = playerPos.distanceTo(lastFootprintPos);
        if (distance < MIN_STEP_DISTANCE) {
            return;
        }

        // Create footprint
        createFootprint(playerPos, player.getYRot(), gameTime);
        lastFootprintPos = playerPos;
        lastYRot = player.getYRot();

        // Clean up expired footprints
        cleanupExpiredFootprints(gameTime);
    }

    /**
     * Checks if the block is a snowy surface that should show footprints.
     */
    private static boolean isSnowySurface(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        // Snow layer
        if (state.is(Blocks.SNOW)) {
            return true;
        }

        // Snow block
        if (state.is(Blocks.SNOW_BLOCK)) {
            return true;
        }

        // Powder snow
        if (state.is(Blocks.POWDER_SNOW)) {
            return true;
        }

        // Check the block below for grass with snow on top
        BlockState above = level.getBlockState(pos.above());
        if (above.is(Blocks.SNOW)) {
            return true;
        }

        return false;
    }

    /**
     * Creates a new footprint at the given position.
     */
    private static void createFootprint(Vec3 playerPos, float yRot, long gameTime) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        // Calculate footprint position offset based on foot
        double radians = Math.toRadians(yRot);
        double perpX = Math.cos(radians + Math.PI / 2);
        double perpZ = Math.sin(radians + Math.PI / 2);

        double offsetMultiplier = nextIsLeftFoot ? -FOOT_OFFSET : FOOT_OFFSET;
        double footX = playerPos.x + perpX * offsetMultiplier;
        double footZ = playerPos.z + perpZ * offsetMultiplier;

        // Calculate proper Y position based on snow layer height
        double footY = calculateSnowSurfaceY(level, footX, footZ, playerPos.y);

        Vec3 footPos = new Vec3(footX, footY, footZ);

        SnowFootprint footprint = new SnowFootprint(
            footPos,
            (float) Math.toRadians(yRot),
            nextIsLeftFoot,
            gameTime,
            FOOTPRINT_LIFETIME
        );

        footprints.add(footprint);
        nextIsLeftFoot = !nextIsLeftFoot;

        // Debug log
        if (footprints.size() <= 5) {
            Auroral.LOGGER.debug("Created footprint at {} (total: {})", footPos, footprints.size());
        }

        // Remove oldest footprints if over limit
        while (footprints.size() > MAX_FOOTPRINTS) {
            footprints.remove(0);
        }
    }

    /**
     * Calculates the Y position for the snow surface at the given XZ coordinates.
     * Accounts for snow layer height (each layer is 0.125 blocks = 2 pixels).
     */
    private static double calculateSnowSurfaceY(Level level, double x, double z, double playerY) {
        BlockPos basePos = BlockPos.containing(x, playerY, z);

        // Check positions around player feet
        for (int yOffset = 1; yOffset >= -2; yOffset--) {
            BlockPos checkPos = basePos.offset(0, yOffset, 0);
            BlockState state = level.getBlockState(checkPos);

            if (state.is(Blocks.SNOW)) {
                // Snow layer - height is based on layers property (1-8)
                // Each layer is 0.125 blocks (2 pixels)
                int layers = state.getValue(SnowLayerBlock.LAYERS);
                double surfaceY = checkPos.getY() + (layers * 0.125) + 0.0625; // +1 pixel for clearance
                return surfaceY;
            } else if (state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
                // Full snow block - surface is at top of block
                return checkPos.getY() + 1.0 + 0.0625; // +1 pixel for clearance
            }
        }

        // Fallback to player position with small offset
        return playerY + 0.001;
    }

    /**
     * Removes expired footprints from the list.
     */
    private static void cleanupExpiredFootprints(long gameTime) {
        Iterator<SnowFootprint> iterator = footprints.iterator();
        while (iterator.hasNext()) {
            SnowFootprint footprint = iterator.next();
            if (footprint.isExpired(gameTime)) {
                iterator.remove();
            }
        }
    }
}
