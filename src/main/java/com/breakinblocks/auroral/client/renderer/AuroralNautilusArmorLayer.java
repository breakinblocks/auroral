package com.breakinblocks.auroral.client.renderer;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.client.model.AuroralNautilusArmorModel;
import com.breakinblocks.auroral.client.model.AuroralNautilusModel;
import com.breakinblocks.auroral.entity.AuroralNautilusEntity;
import com.breakinblocks.auroral.item.ShimmersteelNautilusArmorItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class AuroralNautilusArmorLayer extends RenderLayer<AuroralNautilusEntity, AuroralNautilusModel> {

    private static final ResourceLocation TEXTURE = Auroral.id("textures/entity/auroral_nautilus_armor.png");

    private final AuroralNautilusArmorModel model;

    public AuroralNautilusArmorLayer(RenderLayerParent<AuroralNautilusEntity, AuroralNautilusModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new AuroralNautilusArmorModel(modelSet.bakeLayer(AuroralNautilusArmorModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AuroralNautilusEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!(entity.getBodyArmorItem().getItem() instanceof ShimmersteelNautilusArmorItem)) {
            return;
        }
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F));
    }
}
