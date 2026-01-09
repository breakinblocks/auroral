package com.breakinblocks.auroral.client.renderer;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.client.ClientAuroraState;
import com.breakinblocks.auroral.config.AuroralConfig;
import com.breakinblocks.auroral.util.BiomeHelper;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Custom sky renderer that adds aurora borealis ribbons to the sky.
 * Uses RenderLevelStageEvent to inject rendering after the sky.
 * Rewritten from skylblock modification originally implemented in 1.0.0
 */
@EventBusSubscriber(modid = Auroral.MOD_ID, value = Dist.CLIENT)
public class AuroraSkyRenderer {

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

    private static final int[] tempColor = new int[4];

    @Nullable
    private static GpuBuffer cachedVertexBuffer;
    private static int cachedVertexBufferSize;
    private static float smoothedIntensity = 0.0f;
    private static final float INTENSITY_LERP_SPEED = 0.05f;

    @Nullable
    private static RenderPipeline auroraPipeline;

    private static RenderPipeline getAuroraPipeline() {
        if (auroraPipeline == null) {
            auroraPipeline = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                    .withLocation("auroral/pipeline/aurora")
                    .withVertexShader("core/position_color")
                    .withFragmentShader("core/position_color")
                    .withBlend(new BlendFunction(
                            com.mojang.blaze3d.platform.SourceFactor.SRC_ALPHA,
                            com.mojang.blaze3d.platform.DestFactor.ONE,
                            com.mojang.blaze3d.platform.SourceFactor.ONE,
                            com.mojang.blaze3d.platform.DestFactor.ZERO
                    ))
                    .withCull(false)
                    .withDepthWrite(false)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                    .build();
        }
        return auroraPipeline;
    }

    /**
     * Event handler needed to render aurora after sky rendering.
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterSky event) {
        // Early exit if disabled in config
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

        // Render the aurora
        LevelRenderState levelRenderState = event.getLevelRenderState();
        renderAurora(levelRenderState, smoothedIntensity);
    }

    private static void renderAurora(LevelRenderState levelRenderState, float intensity) {
        long gameTime = levelRenderState.gameTime;
        // Using a modulo here to attempt to prevent float precision loss over time
        float time = (gameTime % (long) TIME_CYCLE) * 0.05f;

        // This was a lot of trial and error getting this working right butttt...
        // We Calculate total vertices needed for all ribbons...
        // Each ribbon has (SEGMENTS + 1) * 2 vertices for the triangle strip
        // Plus 2 vertices between each ribbon pair
        int verticesPerRibbon = (SEGMENTS_PER_RIBBON + 1) * 2;
        int degenerateVertices = 2 * (RIBBON_COUNT - 1);
        int totalVertices = (verticesPerRibbon * RIBBON_COUNT) + degenerateVertices;
        int vertexSize = DefaultVertexFormat.POSITION_COLOR.getVertexSize();
        int bufferSize = totalVertices * vertexSize;

        // Now we build all ribbons into a single mesh
        // Vertices are in camera relative space (camera at 0,0,0)
        MeshData meshData = null;
        try (ByteBufferBuilder byteBuffer = ByteBufferBuilder.exactlySized(bufferSize)) {
            BufferBuilder builder = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int ribbon = 0; ribbon < RIBBON_COUNT; ribbon++) {
                if (ribbon > 0) {
                    addDegenerateConnection(builder, ribbon, time, intensity);
                }
                buildRibbonVertices(builder, ribbon, time, intensity);
            }

            meshData = builder.build();
            if (meshData == null) {
                return;
            }

            // Reuse or create vertex buffer
            GpuBuffer vertexBuffer = getOrCreateVertexBuffer(bufferSize, meshData);
            if (vertexBuffer == null) {
                meshData.close();
                return;
            }

            // Get render target... hopefully
            Minecraft mc = Minecraft.getInstance();
            var renderTarget = mc.getMainRenderTarget();
            if (renderTarget == null) {
                meshData.close();
                return;
            }

            var colorView = renderTarget.getColorTextureView();
            var depthView = renderTarget.getDepthTextureView();
            if (colorView == null) {
                meshData.close();
                return;
            }

            // Create transform uniform using the model view matrix
            GpuBufferSlice transformSlice = RenderSystem.getDynamicUniforms().writeTransform(
                    RenderSystem.getModelViewMatrix(),
                    new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                    new Vector3f(0, 0, 0),
                    new Matrix4f()
            );

            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                    .createRenderPass(
                            () -> "Aurora",
                            colorView,
                            OptionalInt.empty(),
                            depthView,
                            OptionalDouble.empty()
                    )) {
                renderPass.setPipeline(getAuroraPipeline());
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", transformSlice);
                renderPass.setVertexBuffer(0, vertexBuffer);
                renderPass.draw(0, totalVertices);
            }
        } finally {
            if (meshData != null) {
                meshData.close();
            }
        }
    }

    @Nullable
    private static GpuBuffer getOrCreateVertexBuffer(int requiredSize, MeshData meshData) {
        try {
            if (cachedVertexBuffer != null) {
                cachedVertexBuffer.close();
            }
            cachedVertexBufferSize = requiredSize;
            cachedVertexBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "Aurora vertex buffer",
                    GpuBuffer.USAGE_VERTEX,
                    meshData.vertexBuffer()
            );
            return cachedVertexBuffer;
        } catch (Exception e) {
            Auroral.LOGGER.warn("Failed to create aurora vertex buffer", e);
            return null;
        }
    }

    private static void addDegenerateConnection(BufferBuilder builder, int ribbonIndex, float time, float intensity) {
        float ribbonOffset = ribbonIndex * (float) (Math.PI * 2.0 / RIBBON_COUNT);
        float drift = (time * DRIFT_SPEED) % (float) (Math.PI * 2.0) + ribbonIndex * 1.5f;

        float angle = ribbonOffset + drift;
        float x = (float) Math.cos(angle) * RIBBON_RADIUS;
        float z = (float) Math.sin(angle) * RIBBON_RADIUS;
        // Y is relative to camera (0,0,0), so RIBBON_Y_OFFSET puts it above the player
        float yOffset = RIBBON_Y_OFFSET;

        builder.addVertex(x, yOffset, z).setColor(0, 0, 0, 0);
        builder.addVertex(x, yOffset, z).setColor(0, 0, 0, 0);
    }

    private static void buildRibbonVertices(BufferBuilder builder, int ribbonIndex, float time, float intensity) {
        float ribbonOffset = ribbonIndex * (float) (Math.PI * 2.0 / RIBBON_COUNT);
        float colorPhase = ribbonIndex * 0.3f;
        float drift = (time * DRIFT_SPEED) % (float) (Math.PI * 2.0) + ribbonIndex * 1.5f;

        float waveTime1 = time * WAVE_SPEED;
        float waveTime2 = time * WAVE_SPEED * 1.7f;
        float waveTime3 = time * WAVE_SPEED * 0.5f;
        float shimmerTime = time * SHIMMER_SPEED;

        for (int i = 0; i <= SEGMENTS_PER_RIBBON; i++) {
            float segmentProgress = (float) i / SEGMENTS_PER_RIBBON;
            float angle = segmentProgress * (float) (Math.PI * 1.5) + ribbonOffset + drift;

            float x = (float) Math.cos(angle) * RIBBON_RADIUS;
            float z = (float) Math.sin(angle) * RIBBON_RADIUS;

            float wave1 = (float) Math.sin(waveTime1 + segmentProgress * 8.0f + ribbonIndex) * 15.0f;
            float wave2 = (float) Math.sin(waveTime2 + segmentProgress * 12.0f + ribbonIndex * 2.0f) * 8.0f;
            float wave3 = (float) Math.sin(waveTime3 + segmentProgress * 4.0f) * 20.0f;
            // above the player
            float yOffset = wave1 + wave2 + wave3 + RIBBON_Y_OFFSET;

            float heightFactor = (float) Math.sin(segmentProgress * Math.PI) * 0.8f + 0.2f;
            float height = RIBBON_Y_HEIGHT * heightFactor;

            float shimmer = (float) Math.sin(shimmerTime + segmentProgress * 10.0f + colorPhase) * 0.5f + 0.5f;
            float edgeFade = (float) Math.sin(segmentProgress * Math.PI);

            getAuroraColor(segmentProgress + colorPhase, shimmer, intensity * 0.3f, edgeFade);
            builder.addVertex(x, yOffset, z)
                    .setColor(tempColor[0], tempColor[1], tempColor[2], tempColor[3]);

            getAuroraColor(segmentProgress + colorPhase + 0.2f, shimmer, intensity * 0.7f, edgeFade);
            builder.addVertex(x, yOffset + height, z)
                    .setColor(tempColor[0], tempColor[1], tempColor[2], tempColor[3]);
        }
    }

    private static void getAuroraColor(float position, float shimmer, float intensity, float edgeFade) {
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

        tempColor[0] = r;
        tempColor[1] = g;
        tempColor[2] = b;
        tempColor[3] = alpha;
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

    public static void dispose() {
        if (cachedVertexBuffer != null) {
            cachedVertexBuffer.close();
            cachedVertexBuffer = null;
        }
    }
}
