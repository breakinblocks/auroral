package com.breakinblocks.auroral.client.renderer;

import com.breakinblocks.auroral.entity.ThrownShimmerSpear;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renderer for the Thrown Shimmer Spear projectile.
 * Uses the vanilla arrow renderer pattern with a custom texture.
 */
public class ThrownShimmerSpearRenderer extends ArrowRenderer<ThrownShimmerSpear, ThrownShimmerSpearRenderState> {

    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/projectiles/arrow.png");

    public ThrownShimmerSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(ThrownShimmerSpearRenderState state) {
        return TEXTURE;
    }

    @Override
    public ThrownShimmerSpearRenderState createRenderState() {
        return new ThrownShimmerSpearRenderState();
    }

    @Override
    public void extractRenderState(ThrownShimmerSpear entity, ThrownShimmerSpearRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.auroraEmpowered = entity.isAuroraEmpowered();
    }
}
