package com.breakinblocks.auroral.client.renderer;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.client.ClientAuroraState;
import com.breakinblocks.auroral.client.compat.IrisCompat;
import com.breakinblocks.auroral.config.AuroralConfig;
import com.breakinblocks.auroral.util.BiomeHelper;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Custom skybox renderer that adds aurora borealis ribbons to the sky.
 * Uses RenderLevelStageEvent to inject into the rendering pipeline.
 */
@OnlyIn(Dist.CLIENT)
public class AuroraSkyRenderer {

    public static final ResourceLocation AURORA_SKYBOX_ID = Auroral.id("aurora");

    // Aurora ribbon configuration
    private static final int RIBBON_COUNT = 4;
    private static final int SEGMENTS_PER_RIBBON = 48;
    private static final float RIBBON_Y_OFFSET = 80.0f;  // Height ABOVE player
    private static final float RIBBON_Y_HEIGHT = 60.0f;
    private static final float RIBBON_RADIUS = 200.0f;

    // Animation speeds
    private static final float WAVE_SPEED = 0.015f;
    private static final float SHIMMER_SPEED = 0.03f;
    private static final float DRIFT_SPEED = 0.002f;

    // Time cycle to prevent float overflow
    private static final float TIME_CYCLE = 100000.0f;

    private static float smoothedIntensity = 0.0f;
    private static final float INTENSITY_LERP_SPEED = 0.05f;

    /**
     * Called from RenderLevelStageEvent to render the aurora.
     */
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }

        if (IrisCompat.isRenderingShadowPass()) {
            return;
        }

        if (!AuroralConfig.CLIENT.showAuroraEffect.get()) {
            smoothedIntensity = 0.0f;
            return;
        }

        // Check if aurora should be visible
        if (!ClientAuroraState.isAuroraActive()) {
            // Fade out smoothly
            smoothedIntensity = Math.max(0.0f, smoothedIntensity - INTENSITY_LERP_SPEED);
            if (smoothedIntensity <= 0.01f) {
                return;
            }
        }

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }

        // Dimension check - only render in overworld-like dimensions
        if (!level.dimensionType().hasSkyLight()) {
            return;
        }

        // Only render in cold biomes
        if (!BiomeHelper.canExperienceAurora(level, mc.player.blockPosition())) {
            // Fade out when leaving cold biomes
            smoothedIntensity = Math.max(0.0f, smoothedIntensity - INTENSITY_LERP_SPEED);
            if (smoothedIntensity <= 0.01f) {
                return;
            }
        }

        // Calculate aurora intensity based on time of night
        float nightProgress = getNightProgress(level);
        if (nightProgress <= 0 && smoothedIntensity <= 0.01f) {
            return;
        }

        float targetIntensity = calculateIntensity(nightProgress);

        // Apply config intensity multiplier
        targetIntensity *= AuroralConfig.CLIENT.auroraIntensity.get().floatValue();

        // Smooth intensity transitions
        if (targetIntensity > smoothedIntensity) {
            smoothedIntensity = Math.min(targetIntensity, smoothedIntensity + INTENSITY_LERP_SPEED);
        } else {
            smoothedIntensity = Math.max(targetIntensity, smoothedIntensity - INTENSITY_LERP_SPEED);
        }

        if (smoothedIntensity <= 0.01f) {
            return;
        }

        // Render the aurora - we need to cancel out camera rotation so aurora stays fixed in world space
        Camera camera = event.getCamera();
        renderAurora(event.getPoseStack(), event.getProjectionMatrix(), level.getGameTime(), smoothedIntensity, camera);
    }

    private static void renderAurora(PoseStack poseStack, Matrix4f projectionMatrix, long gameTime, float intensity, Camera camera) {
        float time = (gameTime % (long) TIME_CYCLE) * 0.05f;

        AuroraRenderType.AURORA_RIBBON.setupRenderState();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        poseStack.pushPose();

        Quaternionf cameraRotation = camera.rotation();
        Quaternionf inverseRotation = new Quaternionf(cameraRotation).conjugate();
        poseStack.mulPose(inverseRotation);

        Matrix4f matrix = poseStack.last().pose();

        for (int ribbon = 0; ribbon < RIBBON_COUNT; ribbon++) {
            buildRibbonVertices(builder, matrix, ribbon, time, intensity);
        }

        BufferUploader.drawWithShader(builder.buildOrThrow());

        poseStack.popPose();

        AuroraRenderType.AURORA_RIBBON.clearRenderState();
    }

    private static void buildRibbonVertices(BufferBuilder builder, Matrix4f matrix, int ribbonIndex, float time, float intensity) {
        float ribbonOffset = ribbonIndex * (float) (Math.PI * 2.0 / RIBBON_COUNT);
        float colorPhase = ribbonIndex * 0.3f;
        float drift = (time * DRIFT_SPEED) % (float) (Math.PI * 2.0) + ribbonIndex * 1.5f;

        float waveTime1 = time * WAVE_SPEED;
        float waveTime2 = time * WAVE_SPEED * 1.7f;
        float waveTime3 = time * WAVE_SPEED * 0.5f;
        float shimmerTime = time * SHIMMER_SPEED;

        int n = SEGMENTS_PER_RIBBON + 1;
        float[] vX = new float[n];
        float[] vZ = new float[n];
        float[] vYBot = new float[n];
        float[] vYTop = new float[n];
        int[][] vColorBot = new int[n][];
        int[][] vColorTop = new int[n][];

        for (int i = 0; i < n; i++) {
            float segmentProgress = (float) i / SEGMENTS_PER_RIBBON;
            float angle = segmentProgress * (float) (Math.PI * 1.5) + ribbonOffset + drift;

            float x = (float) Math.cos(angle) * RIBBON_RADIUS;
            float z = (float) Math.sin(angle) * RIBBON_RADIUS;

            float wave1 = (float) Math.sin(waveTime1 + segmentProgress * 8.0f + ribbonIndex) * 15.0f;
            float wave2 = (float) Math.sin(waveTime2 + segmentProgress * 12.0f + ribbonIndex * 2.0f) * 8.0f;
            float wave3 = (float) Math.sin(waveTime3 + segmentProgress * 4.0f) * 20.0f;
            float yOffset = wave1 + wave2 + wave3 + RIBBON_Y_OFFSET;

            float heightFactor = (float) Math.sin(segmentProgress * Math.PI) * 0.8f + 0.2f;
            float height = RIBBON_Y_HEIGHT * heightFactor;

            float shimmer = (float) Math.sin(shimmerTime + segmentProgress * 10.0f + colorPhase) * 0.5f + 0.5f;
            float edgeFade = (float) Math.sin(segmentProgress * Math.PI);

            vX[i] = x;
            vZ[i] = z;
            vYBot[i] = yOffset;
            vYTop[i] = yOffset + height;
            vColorBot[i] = getAuroraColor(segmentProgress + colorPhase, shimmer, intensity * 0.3f, edgeFade);
            vColorTop[i] = getAuroraColor(segmentProgress + colorPhase + 0.2f, shimmer, intensity * 0.7f, edgeFade);
        }

        for (int i = 0; i < SEGMENTS_PER_RIBBON; i++) {
            int j = i + 1;
            emitVertex(builder, matrix, vX[i], vYBot[i], vZ[i], vColorBot[i]);
            emitVertex(builder, matrix, vX[j], vYBot[j], vZ[j], vColorBot[j]);
            emitVertex(builder, matrix, vX[j], vYTop[j], vZ[j], vColorTop[j]);

            emitVertex(builder, matrix, vX[i], vYBot[i], vZ[i], vColorBot[i]);
            emitVertex(builder, matrix, vX[j], vYTop[j], vZ[j], vColorTop[j]);
            emitVertex(builder, matrix, vX[i], vYTop[i], vZ[i], vColorTop[i]);
        }
    }

    private static void emitVertex(BufferBuilder builder, Matrix4f matrix, float x, float y, float z, int[] color) {
        builder.addVertex(matrix, x, y, z).setColor(color[0], color[1], color[2], color[3]);
    }

    private static int[] getAuroraColor(float position, float shimmer, float intensity, float edgeFade) {
        position = position - (float) Math.floor(position);

        int r, g, b;

        if (position < 0.25f) {
            float t = position / 0.25f;
            r = (int) lerp(t, 50, 80);
            g = (int) lerp(t, 220, 230);
            b = (int) lerp(t, 100, 200);
        } else if (position < 0.5f) {
            float t = (position - 0.25f) / 0.25f;
            r = (int) lerp(t, 80, 200);
            g = (int) lerp(t, 230, 100);
            b = (int) lerp(t, 200, 220);
        } else if (position < 0.75f) {
            float t = (position - 0.5f) / 0.25f;
            r = (int) lerp(t, 200, 255);
            g = (int) lerp(t, 100, 150);
            b = (int) lerp(t, 220, 200);
        } else {
            float t = (position - 0.75f) / 0.25f;
            r = (int) lerp(t, 255, 50);
            g = (int) lerp(t, 150, 220);
            b = (int) lerp(t, 200, 100);
        }

        float shimmerBoost = 0.8f + shimmer * 0.4f;
        r = clamp((int) (r * shimmerBoost), 0, 255);
        g = clamp((int) (g * shimmerBoost), 0, 255);
        b = clamp((int) (b * shimmerBoost), 0, 255);

        int alpha = clamp((int) (intensity * edgeFade * 180), 0, 255);

        return new int[]{r, g, b, alpha};
    }

    private static float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float getNightProgress(ClientLevel level) {
        long dayTime = level.getDayTime() % 24000;
        if (dayTime < 13000 || dayTime >= 23000) {
            return 0;
        }
        return (dayTime - 13000) / 10000.0f;
    }

    private static float calculateIntensity(float nightProgress) {
        if (nightProgress < 0.1f) {
            return nightProgress / 0.1f;
        }
        if (nightProgress > 0.9f) {
            return (1.0f - nightProgress) / 0.1f;
        }
        return 1.0f;
    }
}
