package com.breakinblocks.auroral.client.renderer;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.entity.StarShotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Renderer for the Star-Shot projectile.
 * Renders a glowing textured billboard sprite that always faces the camera.
 */
public class StarShotRenderer extends EntityRenderer<StarShotEntity> {

    private static final ResourceLocation TEXTURE = Auroral.id("textures/entity/star_shot.png");
    private static final float SIZE = 0.5f;

    public StarShotRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(StarShotEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Make the sprite always face the camera (billboard effect)
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        // Flip to face forward
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        // Pulsing glow effect based on age
        float ageInTicks = entity.tickCount + partialTick;
        float pulse = 1.0f + 0.1f * (float) Math.sin(ageInTicks * 0.3);
        poseStack.scale(SIZE * pulse, SIZE * pulse, SIZE * pulse);

        // Use translucent emissive render type for glow effect
        RenderType renderType = RenderType.entityTranslucentEmissive(TEXTURE);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        // Full brightness for emissive glow
        int light = 0xF000F0;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        // Render a quad facing the camera
        // Vertices in counter-clockwise order
        consumer.addVertex(matrix, -0.5F, -0.5F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        consumer.addVertex(matrix, 0.5F, -0.5F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        consumer.addVertex(matrix, 0.5F, 0.5F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        consumer.addVertex(matrix, -0.5F, 0.5F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(StarShotEntity entity) {
        return TEXTURE;
    }
}
