package com.breakinblocks.auroral.client;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.client.footprint.SnowFootprintManager;
import com.breakinblocks.auroral.client.footprint.SnowFootprintRenderer;
import com.breakinblocks.auroral.client.gui.ColdBrewingStandScreen;
import com.breakinblocks.auroral.client.model.AuroralNautilusArmorModel;
import com.breakinblocks.auroral.client.model.AuroralNautilusModel;
import com.breakinblocks.auroral.client.particle.AuroraSparkleParticle;
import com.breakinblocks.auroral.client.particle.ShimmerParticle;
import com.breakinblocks.auroral.client.renderer.AuroralNautilusRenderer;
import com.breakinblocks.auroral.client.renderer.AuroralSnowletteRenderer;
import com.breakinblocks.auroral.client.renderer.AuroraSkyRenderer;
import com.breakinblocks.auroral.client.renderer.ShimmerweaveVisorLayer;
import com.breakinblocks.auroral.client.renderer.StarShotRenderer;
import com.breakinblocks.auroral.client.renderer.ThrownShimmerSpearRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.EntityType;
import com.breakinblocks.auroral.registry.ModEntities;
import com.breakinblocks.auroral.registry.ModMenuTypes;
import com.breakinblocks.auroral.registry.ModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Auroral.MOD_ID, value = Dist.CLIENT)
public class AuroralClient {

    public static void init(IEventBus eventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (IConfigScreenFactory) ConfigurationScreen::new);
        eventBus.addListener(AuroralClient::registerLayerDefinitions);
        eventBus.addListener(AuroralClient::registerRenderers);
        eventBus.addListener(AuroralClient::registerParticleProviders);
        eventBus.addListener(AuroralClient::registerMenuScreens);
        eventBus.addListener(AuroralClient::addEntityLayers);

        Auroral.LOGGER.debug("Auroral client initialized");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        var modelSet = event.getContext().getModelSet();
        for (var skin : event.getSkins()) {
            AvatarRenderer<?> playerRenderer = event.getPlayerRenderer(skin);
            if (playerRenderer != null) {
                playerRenderer.addLayer(new ShimmerweaveVisorLayer(playerRenderer, modelSet));
            }
            AvatarRenderer<?> mannequinRenderer = event.getMannequinRenderer(skin);
            if (mannequinRenderer != null) {
                mannequinRenderer.addLayer(new ShimmerweaveVisorLayer(mannequinRenderer, modelSet));
            }
        }
        for (EntityType<?> type : event.getEntityTypes()) {
            EntityRenderer<?, ?> renderer = event.getRenderer(type);
            if (renderer instanceof LivingEntityRenderer livingRenderer
                    && livingRenderer.getModel() instanceof HumanoidModel) {
                livingRenderer.addLayer(new ShimmerweaveVisorLayer(livingRenderer, modelSet));
            }
        }
    }

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AuroralNautilusModel.LAYER_LOCATION, AuroralNautilusModel::createBodyLayer);
        event.registerLayerDefinition(AuroralNautilusArmorModel.LAYER_LOCATION, AuroralNautilusArmorModel::createBodyLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.AURORAL_NAUTILUS.get(), AuroralNautilusRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWN_SHIMMER_SPEAR.get(), ThrownShimmerSpearRenderer::new);
        event.registerEntityRenderer(ModEntities.STAR_SHOT.get(), StarShotRenderer::new);
        event.registerEntityRenderer(ModEntities.AURORAL_SNOWLETTE.get(), AuroralSnowletteRenderer::new);
    }

    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.AURORA_SPARKLE.get(), AuroraSparkleParticle.Provider::new);
        event.registerSpriteSet(ModParticles.BASIN_INFUSE.get(), ShimmerParticle.BasinInfuseProvider::new);
        event.registerSpriteSet(ModParticles.STAR_TRAIL.get(), ShimmerParticle.StarTrailProvider::new);
        event.registerSpriteSet(ModParticles.FROST.get(), ShimmerParticle.FrostProvider::new);
        event.registerSpriteSet(ModParticles.SHIMMER.get(), ShimmerParticle.ShimmerProvider::new);
    }

    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.COLD_BREWING_STAND.get(), ColdBrewingStandScreen::new);
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientAuroraState.reset();
        SnowFootprintManager.clear();
        AuroraMusicHandler.forceStop();
        SnowFootprintRenderer.dispose();
        AuroraSkyRenderer.dispose();
    }
}
