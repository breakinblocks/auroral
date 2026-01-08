package com.breakinblocks.auroral.client.model;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.client.renderer.AuroralNautilusRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Model for the Auroral Nautilus.
 * Exact copy of vanilla NautilusModel geometry for proper texture mapping.
 */
public class AuroralNautilusModel extends EntityModel<AuroralNautilusRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
        new ModelLayerLocation(Auroral.id("auroral_nautilus"), "main");

    protected final ModelPart body;
    protected final ModelPart nautilus;

    public AuroralNautilusModel(ModelPart modelPart) {
        super(modelPart);
        this.nautilus = modelPart.getChild("root");
        this.body = this.nautilus.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 29.0F, -6.0F));
        partdefinition1.addOrReplaceChild(
            "shell",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-7.0F, -10.0F, -7.0F, 14.0F, 10.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26)
                .addBox(-7.0F, 0.0F, -7.0F, 14.0F, 8.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(48, 26)
                .addBox(-7.0F, 0.0F, 6.0F, 14.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -13.0F, 5.0F)
        );
        PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(0, 54)
                .addBox(-5.0F, -4.51F, -3.0F, 10.0F, 8.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 76)
                .addBox(-5.0F, -4.51F, 7.0F, 10.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -8.5F, 12.3F)
        );
        partdefinition2.addOrReplaceChild(
            "upper_mouth",
            CubeListBuilder.create().texOffs(54, 54).addBox(-5.0F, -2.0F, 0.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-0.001F)),
            PartPose.offset(0.0F, -2.51F, 7.0F)
        );
        partdefinition2.addOrReplaceChild(
            "inner_mouth",
            CubeListBuilder.create().texOffs(54, 70).addBox(-3.0F, -2.0F, -0.5F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -0.51F, 7.5F)
        );
        partdefinition2.addOrReplaceChild(
            "lower_mouth",
            CubeListBuilder.create().texOffs(54, 62).addBox(-5.0F, -1.98F, 0.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-0.001F)),
            PartPose.offset(0.0F, 1.49F, 7.0F)
        );
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(AuroralNautilusRenderState renderState) {
        super.setupAnim(renderState);
        this.applyBodyRotation(renderState.yRot, renderState.xRot);
    }

    private void applyBodyRotation(float yRot, float xRot) {
        yRot = Mth.clamp(yRot, -10.0F, 10.0F);
        xRot = Mth.clamp(xRot, -10.0F, 10.0F);
        this.body.yRot = yRot * ((float) Math.PI / 180.0F);
        this.body.xRot = xRot * ((float) Math.PI / 180.0F);
    }
}
