package com.breakinblocks.auroral.client;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.client.footprint.SnowFootprintManager;
import com.breakinblocks.auroral.client.footprint.SnowFootprintRenderer;
import com.breakinblocks.auroral.client.gui.ColdBrewingStandScreen;
import com.breakinblocks.auroral.client.model.AuroralNautilusModel;
import com.breakinblocks.auroral.client.particle.AuroraSparkleParticle;
import com.breakinblocks.auroral.client.particle.ShimmerParticle;
import com.breakinblocks.auroral.client.renderer.AuroralNautilusRenderer;
import com.breakinblocks.auroral.client.renderer.AuroralSnowletteRenderer;
import com.breakinblocks.auroral.client.renderer.AuroraSkyRenderer;
import com.breakinblocks.auroral.client.renderer.StarShotRenderer;
import com.breakinblocks.auroral.client.renderer.ThrownShimmerSpearRenderer;
import com.breakinblocks.auroral.registry.ModEntities;
import com.breakinblocks.auroral.registry.ModMenuTypes;
import com.breakinblocks.auroral.registry.ModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * Client-side initialization and event handling.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID, value = Dist.CLIENT)
public class AuroralClient {

    /**
     * Initialize client-side systems.
     * Called from main mod class during construction.
     *
     * @param eventBus The mod event bus
     */
    public static void init(IEventBus eventBus) {
        // Register client-specific event listeners on the MOD bus
        eventBus.addListener(AuroralClient::registerLayerDefinitions);
        eventBus.addListener(AuroralClient::registerRenderers);
        eventBus.addListener(AuroralClient::registerParticleProviders);
        eventBus.addListener(AuroralClient::registerCustomRenderers);
        eventBus.addListener(AuroralClient::registerMenuScreens);

        Auroral.LOGGER.debug("Auroral client initialized");
    }

    /**
     * Register model layer definitions.
     */
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AuroralNautilusModel.LAYER_LOCATION, AuroralNautilusModel::createBodyLayer);
    }

    /**
     * Register entity renderers.
     */
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.AURORAL_NAUTILUS.get(), AuroralNautilusRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWN_SHIMMER_SPEAR.get(), ThrownShimmerSpearRenderer::new);
        event.registerEntityRenderer(ModEntities.STAR_SHOT.get(), StarShotRenderer::new);
        event.registerEntityRenderer(ModEntities.AURORAL_SNOWLETTE.get(), AuroralSnowletteRenderer::new);
    }

    /**
     * Register particle providers for custom particles.
     */
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.AURORA_SPARKLE.get(), AuroraSparkleParticle.Provider::new);
        event.registerSpriteSet(ModParticles.BASIN_INFUSE.get(), ShimmerParticle.BasinInfuseProvider::new);
        event.registerSpriteSet(ModParticles.STAR_TRAIL.get(), ShimmerParticle.StarTrailProvider::new);
        event.registerSpriteSet(ModParticles.FROST.get(), ShimmerParticle.FrostProvider::new);
        event.registerSpriteSet(ModParticles.SHIMMER.get(), ShimmerParticle.ShimmerProvider::new);
    }

    /**
     * Register menu screens for custom menus.
     */
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.COLD_BREWING_STAND.get(), ColdBrewingStandScreen::new);
    }

    // Keep reference to aurora renderer for cleanup
    private static AuroraSkyRenderer auroraSkyRenderer;

    /**
     * Register custom environment effect renderers (aurora skybox).
     */
    public static void registerCustomRenderers(RegisterCustomEnvironmentEffectRendererEvent event) {
        auroraSkyRenderer = new AuroraSkyRenderer();
        event.registerSkyboxRenderer(AuroraSkyRenderer.AURORA_SKYBOX_ID, auroraSkyRenderer);
        Auroral.LOGGER.debug("Registered aurora skybox renderer");
    }

    /**
     * Reset client state when disconnecting from server.
     * Releases GPU resources and clears cached state.
     */
    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientAuroraState.reset();
        SnowFootprintManager.clear();
        AuroraMusicHandler.forceStop();

        // Release GPU resources
        SnowFootprintRenderer.dispose();
        if (auroraSkyRenderer != null) {
            auroraSkyRenderer.dispose();
        }
    }
}
