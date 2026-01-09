package com.breakinblocks.auroral.client.renderer;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.entity.AuroralSnowletteEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the Auroral Snowlette - uses Snow Golem model scaled to 1/2 size
 * with a custom aurora-tinted texture.
 */
public class AuroralSnowletteRenderer extends MobRenderer<AuroralSnowletteEntity, SnowGolemModel<AuroralSnowletteEntity>> {

    private static final ResourceLocation TEXTURE = Auroral.id("textures/entity/auroral_snowlette.png");
    private static final float SCALE = 0.5F; // 1/2 size

    public AuroralSnowletteRenderer(EntityRendererProvider.Context context) {
        super(context, new SnowGolemModel<>(context.bakeLayer(ModelLayers.SNOW_GOLEM)), 0.2F); // Small shadow
    }

    @Override
    public ResourceLocation getTextureLocation(AuroralSnowletteEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AuroralSnowletteEntity entity, PoseStack poseStack, float partialTick) {
        // Scale down to 1/2 size
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
