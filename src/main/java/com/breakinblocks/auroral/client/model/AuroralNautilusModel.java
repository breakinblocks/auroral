package com.breakinblocks.auroral.client.model;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.client.renderer.AuroralNautilusRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Model for the Auroral Nautilus - a mystical flying nautilus creature.
 * Based on the vanilla nautilus shell structure but adapted for flying.
 */
public class AuroralNautilusModel extends EntityModel<AuroralNautilusRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
        new ModelLayerLocation(Auroral.id("auroral_nautilus"), "main");

    private final ModelPart root;
    private final ModelPart shell;
    private final ModelPart body;
    private final ModelPart tentacles;

    public AuroralNautilusModel(ModelPart modelPart) {
        super(modelPart);
        this.root = modelPart.getChild("root");
        this.shell = this.root.getChild("shell");
        this.body = this.root.getChild("body");
        this.tentacles = this.body.getChild("tentacles");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Root - offset to center the model
        PartDefinition root = partdefinition.addOrReplaceChild("root",
            CubeListBuilder.create(),
            PartPose.offset(0.0F, 16.0F, 0.0F));

        // Shell - the iconic spiral shell
        root.addOrReplaceChild("shell",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(44, 0)
                .addBox(-4.0F, -3.0F, -8.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        // Body - the soft body emerging from shell
        PartDefinition body = root.addOrReplaceChild("body",
            CubeListBuilder.create()
                .texOffs(0, 20)
                .addBox(-3.0F, -2.5F, 0.0F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 6.0F));

        // Tentacles - flowing appendages
        body.addOrReplaceChild("tentacles",
            CubeListBuilder.create()
                .texOffs(28, 20)
                .addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(44, 10)
                .addBox(-1.0F, -0.5F, 6.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 8.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(AuroralNautilusRenderState renderState) {
        super.setupAnim(renderState);

        // Gentle bobbing motion
        float ageInTicks = renderState.ageInTicks;
        float bob = Mth.sin(ageInTicks * 0.1F) * 0.1F;
        this.root.y = 16.0F + bob * 2.0F;

        // Tentacle sway animation
        float sway = Mth.sin(ageInTicks * 0.15F) * 0.15F;
        this.tentacles.xRot = sway;
        this.tentacles.yRot = Mth.sin(ageInTicks * 0.08F) * 0.1F;

        // Body rotation based on movement
        this.body.xRot = renderState.xRot * ((float) Math.PI / 180F) * 0.3F;
        this.root.yRot = renderState.yRot * ((float) Math.PI / 180F);

        // Shell slight tilt
        this.shell.zRot = Mth.sin(ageInTicks * 0.05F) * 0.05F;
    }
}
