package com.breakinblocks.auroral.client.renderer;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.client.model.AuroralNautilusModel;
import com.breakinblocks.auroral.entity.AuroralNautilusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the Auroral Nautilus entity.
 */
public class AuroralNautilusRenderer extends MobRenderer<AuroralNautilusEntity, AuroralNautilusModel> {

    private static final ResourceLocation TEXTURE = Auroral.id("textures/entity/auroral_nautilus.png");

    public AuroralNautilusRenderer(EntityRendererProvider.Context context) {
        super(context, new AuroralNautilusModel(context.bakeLayer(AuroralNautilusModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(AuroralNautilusEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AuroralNautilusEntity entity, PoseStack poseStack, float partialTick) {
        // Apply size scaling based on nautilus size variant
        float scale = 1.0F + 0.2F * entity.getNautilusSize();
        poseStack.scale(scale, scale, scale);
    }
}
