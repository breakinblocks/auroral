package com.breakinblocks.auroral.client.footprint;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.config.AuroralConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Renderer for snow footprints using 1.21.1 Tesselator API.
 */
@OnlyIn(Dist.CLIENT)
public class SnowFootprintRenderer {

    private static final ResourceLocation FOOTPRINT_TEXTURE = Auroral.id("textures/misc/footprint.png");
    private static final float FOOTPRINT_WIDTH = 0.15f;
    private static final float FOOTPRINT_LENGTH = 0.25f;
    private static final double MAX_RENDER_DISTANCE_SQ = 48.0 * 48.0;
    private static final int MAX_VISIBLE_FOOTPRINTS = 256;

    private static final float[] footprintData = new float[MAX_VISIBLE_FOOTPRINTS * 7];

    /**
     * Called from RenderLevelStageEvent to render footprints.
     */
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // Render after particles for proper layering
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        // Early exit if disabled in config
        if (!AuroralConfig.CLIENT.showFootprints.get()) {
            return;
        }

        List<SnowFootprint> footprints = SnowFootprintManager.getFootprints();
        if (footprints.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        long gameTime = mc.level.getGameTime();

        // Collect visible footprints into reusable array
        int visibleCount = collectVisibleFootprints(footprints, camPos, gameTime);

        if (visibleCount == 0) {
            return;
        }

        renderFootprints(event.getPoseStack(), camPos, visibleCount);
    }

    private static int collectVisibleFootprints(List<SnowFootprint> footprints, Vec3 camPos, long gameTime) {
        int visibleCount = 0;

        for (SnowFootprint footprint : footprints) {
            if (visibleCount >= MAX_VISIBLE_FOOTPRINTS) break;

            // Distance culling
            double dx = camPos.x - footprint.position.x;
            double dy = camPos.y - footprint.position.y;
            double dz = camPos.z - footprint.position.z;
            double distSq = dx * dx + dy * dy + dz * dz;

            if (distSq > MAX_RENDER_DISTANCE_SQ) {
                continue;
            }

            float opacity = footprint.getOpacity(gameTime);
            if (opacity <= 0.01f) {
                continue;
            }

            // Distance fade
            float distanceFade = 1.0f - (float) (distSq / MAX_RENDER_DISTANCE_SQ);
            float finalOpacity = opacity * distanceFade;
            if (finalOpacity < 0.02f) {
                continue;
            }

            int idx = visibleCount * 7;
            footprintData[idx] = (float) footprint.position.x;
            footprintData[idx + 1] = (float) footprint.position.y;
            footprintData[idx + 2] = (float) footprint.position.z;
            footprintData[idx + 3] = footprint.rotation;
            footprintData[idx + 4] = finalOpacity;
            footprintData[idx + 5] = footprint.isLeftFoot ? 1.0f : 0.0f;
            footprintData[idx + 6] = distanceFade;
            visibleCount++;
        }

        return visibleCount;
    }

    private static void renderFootprints(PoseStack poseStack, Vec3 camPos, int count) {
        // Setup render state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, FOOTPRINT_TEXTURE);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < count; i++) {
            int idx = i * 7;
            float x = footprintData[idx] - (float) camPos.x;
            float y = footprintData[idx + 1] - (float) camPos.y;
            float z = footprintData[idx + 2] - (float) camPos.z;
            float rotation = footprintData[idx + 3];
            float opacity = footprintData[idx + 4];
            boolean isLeft = footprintData[idx + 5] > 0.5f;

            // Get block height offset for proper placement
            float yOffset = getFootprintYOffset(y);

            addFootprintQuad(builder, matrix, x, yOffset, z, rotation, opacity, isLeft);
        }

        BufferUploader.drawWithShader(builder.buildOrThrow());

        // Restore render state
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static float getFootprintYOffset(float baseY) {
        // Small offset above surface to prevent z-fighting
        return baseY + 0.005f;
    }

    private static void addFootprintQuad(BufferBuilder builder, Matrix4f matrix, float x, float y, float z,
                                         float rotation, float opacity, boolean isLeft) {
        float cos = (float) Math.cos(rotation);
        float sin = (float) Math.sin(rotation);

        float hw = FOOTPRINT_WIDTH;
        float hl = FOOTPRINT_LENGTH;

        // Calculate rotated corners (front-left, front-right, back-right, back-left)
        // Pre-compute products to reduce multiplications
        float hwCos = hw * cos;
        float hwSin = hw * sin;
        float hlCos = hl * cos;
        float hlSin = hl * sin;

        float c0x = -hwCos - hlSin;
        float c0z = -hwSin + hlCos;
        float c1x = hwCos - hlSin;
        float c1z = hwSin + hlCos;
        float c2x = hwCos + hlSin;
        float c2z = hwSin - hlCos;
        float c3x = -hwCos + hlSin;
        float c3z = -hwSin - hlCos;

        // UV coordinates - flip for left foot
        float u0 = isLeft ? 1.0f : 0.0f;
        float u1 = isLeft ? 0.0f : 1.0f;

        // Dark gray color for footprint depression
        int r = 60;
        int g = 60;
        int b = 70;
        int alpha = clamp((int) (opacity * 180), 0, 255);

        // Add quad vertices
        builder.addVertex(matrix, x + c0x, y, z + c0z)
                .setUv(u0, 0.0f)
                .setColor(r, g, b, alpha);
        builder.addVertex(matrix, x + c1x, y, z + c1z)
                .setUv(u1, 0.0f)
                .setColor(r, g, b, alpha);
        builder.addVertex(matrix, x + c2x, y, z + c2z)
                .setUv(u1, 1.0f)
                .setColor(r, g, b, alpha);
        builder.addVertex(matrix, x + c3x, y, z + c3z)
                .setUv(u0, 1.0f)
                .setColor(r, g, b, alpha);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Dispose any cached resources. In 1.21.1 we use immediate mode so nothing to clean up.
     */
    public static void dispose() {
        // No GPU resources to clean up in 1.21.1 immediate mode rendering
    }
}
