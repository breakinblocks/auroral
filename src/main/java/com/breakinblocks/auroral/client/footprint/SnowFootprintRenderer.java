package com.breakinblocks.auroral.client.footprint;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.config.AuroralConfig;
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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

@EventBusSubscriber(modid = Auroral.MOD_ID, value = Dist.CLIENT)
public class SnowFootprintRenderer {

    private static final Identifier FOOTPRINT_TEXTURE = Auroral.id("textures/misc/footprint.png");
    private static final float FOOTPRINT_WIDTH = 0.15f;
    private static final float FOOTPRINT_LENGTH = 0.25f;
    private static final double MAX_RENDER_DISTANCE_SQ = 48.0 * 48.0;
    private static final int MAX_VISIBLE_FOOTPRINTS = 256;

    private static final float[] footprintData = new float[MAX_VISIBLE_FOOTPRINTS * 7];

    @Nullable
    private static GpuBuffer cachedVertexBuffer;
    private static int cachedVertexBufferSize;

    @Nullable
    private static RenderPipeline footprintPipeline;

    private static RenderPipeline getFootprintPipeline() {
        if (footprintPipeline == null) {
            footprintPipeline = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                    .withLocation("auroral/pipeline/footprint")
                    .withVertexShader("core/position_tex_color")
                    .withFragmentShader("core/position_tex_color")
                    .withSampler("Sampler0")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withCull(false)
                    .withDepthWrite(false)
                    .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                    .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                    .build();
        }
        return footprintPipeline;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterParticles event) {
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

        LevelRenderState levelState = event.getLevelRenderState();
        Vec3 camPos = levelState.cameraRenderState.pos;
        long gameTime = levelState.gameTime;

        // Collect visible footprints into reusable array
        int visibleCount = collectVisibleFootprints(footprints, camPos, gameTime);

        if (visibleCount == 0) {
            return;
        }

        renderFootprints(camPos, visibleCount);
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

    private static void renderFootprints(Vec3 camPos, int count) {
        // Calculate buffer size: 4 vertices per footprint
        int vertexSize = DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize();
        int bufferSize = count * 4 * vertexSize;

        MeshData meshData = null;
        try (ByteBufferBuilder byteBuffer = ByteBufferBuilder.exactlySized(bufferSize)) {
            BufferBuilder builder = new BufferBuilder(byteBuffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

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

                addFootprintQuad(builder, x, yOffset, z, rotation, opacity, isLeft);
            }

            meshData = builder.build();
            if (meshData == null) {
                return;
            }

            // Get or create vertex buffer
            GpuBuffer vertexBuffer = getOrCreateVertexBuffer(bufferSize, meshData);
            if (vertexBuffer == null) {
                meshData.close();
                return;
            }

            // Get render target safely
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

            // Get texture safely
            AbstractTexture texture = mc.getTextureManager().getTexture(FOOTPRINT_TEXTURE);
            if (texture == null) {
                meshData.close();
                return;
            }

            RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
            GpuBuffer indexBuffer = quadIndices.getBuffer(count * 6);

            // Create transform uniform
            GpuBufferSlice transformSlice = RenderSystem.getDynamicUniforms().writeTransform(
                    RenderSystem.getModelViewMatrix(),
                    new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                    new Vector3f(0, 0, 0),
                    new Matrix4f()
            );

            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                    .createRenderPass(
                            () -> "Footprints",
                            colorView,
                            OptionalInt.empty(),
                            depthView,
                            OptionalDouble.empty()
                    )) {
                renderPass.setPipeline(getFootprintPipeline());
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", transformSlice);
                renderPass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
                renderPass.setVertexBuffer(0, vertexBuffer);
                renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
                renderPass.drawIndexed(0, 0, count * 6, 1);
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
            // Create buffer with initial data - size is derived from ByteBuffer
            cachedVertexBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "Footprint vertex buffer",
                    GpuBuffer.USAGE_VERTEX,
                    meshData.vertexBuffer()
            );
            cachedVertexBufferSize = requiredSize;
            return cachedVertexBuffer;
        } catch (Exception e) {
            Auroral.LOGGER.warn("Failed to create footprint vertex buffer", e);
            return null;
        }
    }

    private static float getFootprintYOffset(float baseY) {
        // Small offset above surface to prevent z-fighting
        return baseY + 0.005f;
    }

    private static void addFootprintQuad(BufferBuilder builder, float x, float y, float z,
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
        builder.addVertex(x + c0x, y, z + c0z)
                .setUv(u0, 0.0f)
                .setColor(r, g, b, alpha);
        builder.addVertex(x + c1x, y, z + c1z)
                .setUv(u1, 0.0f)
                .setColor(r, g, b, alpha);
        builder.addVertex(x + c2x, y, z + c2z)
                .setUv(u1, 1.0f)
                .setColor(r, g, b, alpha);
        builder.addVertex(x + c3x, y, z + c3z)
                .setUv(u0, 1.0f)
                .setColor(r, g, b, alpha);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void dispose() {
        if (cachedVertexBuffer != null) {
            cachedVertexBuffer.close();
            cachedVertexBuffer = null;
            cachedVertexBufferSize = 0;
        }
    }
}
