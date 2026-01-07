package com.breakinblocks.auroral.client.renderer;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.entity.StarShotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Renderer for the Star-Shot projectile.
 * Renders a glowing textured billboard sprite that always faces the camera.
 */
public class StarShotRenderer extends EntityRenderer<StarShotEntity, StarShotRenderState> {

    private static final Identifier TEXTURE = Auroral.id("textures/entity/star_shot.png");
    private static final float SIZE = 0.5f;

    public StarShotRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public StarShotRenderState createRenderState() {
        return new StarShotRenderState();
    }

    @Override
    public void extractRenderState(StarShotEntity entity, StarShotRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.xRot = entity.getXRot();
        state.yRot = entity.getYRot();
        state.ageInTicks = entity.tickCount + partialTick;
    }

    @Override
    public void submit(StarShotRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();

        // Make the sprite always face the camera (billboard effect)
        poseStack.mulPose(cameraState.orientation);
        // Flip to face forward
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        // Pulsing glow effect based on age
        float pulse = 1.0f + 0.1f * (float) Math.sin(state.ageInTicks * 0.3);
        poseStack.scale(SIZE * pulse, SIZE * pulse, SIZE * pulse);

        // Use translucent emissive render type for glow effect
        var renderType = RenderTypes.entityTranslucentEmissive(TEXTURE);

        // Full brightness for emissive glow
        int light = 0xF000F0;

        // Submit the custom geometry
        collector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            renderQuad(vertexConsumer, pose, light);
        });

        poseStack.popPose();

        super.submit(state, poseStack, collector, cameraState);
    }

    private void renderQuad(VertexConsumer consumer, PoseStack.Pose pose, int light) {
        // Calculate UV coordinates
        float minU = 0.0F;
        float maxU = 1.0F;
        float minV = 0.0F;
        float maxV = 1.0F;

        // Render a quad facing the camera
        // Vertices in counter-clockwise order
        consumer.addVertex(pose, -0.5F, -0.5F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(minU, maxV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        consumer.addVertex(pose, 0.5F, -0.5F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(maxU, maxV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        consumer.addVertex(pose, 0.5F, 0.5F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(maxU, minV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        consumer.addVertex(pose, -0.5F, 0.5F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(minU, minV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
