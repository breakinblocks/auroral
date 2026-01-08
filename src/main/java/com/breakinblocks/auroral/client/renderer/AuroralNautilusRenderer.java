package com.breakinblocks.auroral.client.renderer;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.client.model.AuroralNautilusModel;
import com.breakinblocks.auroral.entity.AuroralNautilusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/**
 * Renderer for the Auroral Nautilus entity.
 */
public class AuroralNautilusRenderer extends MobRenderer<AuroralNautilusEntity, AuroralNautilusRenderState, AuroralNautilusModel> {

    private static final Identifier TEXTURE = Auroral.id("textures/entity/auroral_nautilus.png");

    public AuroralNautilusRenderer(EntityRendererProvider.Context context) {
        super(context, new AuroralNautilusModel(context.bakeLayer(AuroralNautilusModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public Identifier getTextureLocation(AuroralNautilusRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public AuroralNautilusRenderState createRenderState() {
        return new AuroralNautilusRenderState();
    }

    @Override
    public void extractRenderState(AuroralNautilusEntity entity, AuroralNautilusRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.size = entity.getNautilusSize();
    }

    @Override
    protected void scale(AuroralNautilusRenderState renderState, PoseStack poseStack) {
        // Apply size scaling based on nautilus size variant
        float scale = 1.0F + 0.2F * renderState.size;
        poseStack.scale(scale, scale, scale);
    }
}
