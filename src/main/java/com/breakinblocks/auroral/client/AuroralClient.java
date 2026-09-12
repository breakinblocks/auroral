package com.breakinblocks.auroral.client;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.client.footprint.SnowFootprintManager;
import com.breakinblocks.auroral.client.footprint.SnowFootprintRenderer;
import com.breakinblocks.auroral.client.gui.ColdBrewingStandScreen;
import com.breakinblocks.auroral.client.model.AuroralNautilusArmorModel;
import com.breakinblocks.auroral.client.model.AuroralNautilusModel;
import com.breakinblocks.auroral.client.particle.AuroraSparkleParticle;
import com.breakinblocks.auroral.client.particle.ShimmerParticle;
import com.breakinblocks.auroral.client.renderer.AuroraSkyRenderer;
import com.breakinblocks.auroral.client.renderer.AuroralNautilusRenderer;
import com.breakinblocks.auroral.client.renderer.AuroralSnowletteRenderer;
import com.breakinblocks.auroral.client.renderer.ShimmerweaveVisorLayer;
import com.breakinblocks.auroral.client.renderer.StarShotRenderer;
import com.breakinblocks.auroral.item.ShimmersteelBowItem;
import com.breakinblocks.auroral.registry.ModEntities;
import com.breakinblocks.auroral.registry.ModItems;
import com.breakinblocks.auroral.registry.ModMenuTypes;
import com.breakinblocks.auroral.registry.ModParticles;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

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
    public static void init(IEventBus eventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (IConfigScreenFactory) ConfigurationScreen::new);
        // Register client-specific event listeners on the MOD bus
        eventBus.addListener(AuroralClient::clientSetup);
        eventBus.addListener(AuroralClient::registerLayerDefinitions);
        eventBus.addListener(AuroralClient::registerRenderers);
        eventBus.addListener(AuroralClient::addEntityLayers);
        eventBus.addListener(AuroralClient::registerParticleProviders);
        eventBus.addListener(AuroralClient::registerMenuScreens);

        // Register render event listeners on the FORGE/NeoForge event bus
        NeoForge.EVENT_BUS.addListener(AuroraSkyRenderer::onRenderLevelStage);
        NeoForge.EVENT_BUS.addListener(SnowFootprintRenderer::onRenderLevel);

        Auroral.LOGGER.debug("Auroral client initialized");
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            var bow = ModItems.SHIMMERSTEEL_BOW.get();
            ItemProperties.register(bow,
                ResourceLocation.withDefaultNamespace("pull"),
                (stack, level, entity, seed) -> entity != null && entity.getUseItem() == stack
                    ? (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks())
                        / (float) ShimmersteelBowItem.MAX_DRAW_DURATION : 0.0F);
            ItemProperties.register(bow,
                ResourceLocation.withDefaultNamespace("pulling"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem()
                    && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        EntityModelSet modelSet = event.getEntityModels();
        for (PlayerSkin.Model skin : event.getSkins()) {
            EntityRenderer<?> playerRenderer = event.getSkin(skin);
            if (playerRenderer instanceof LivingEntityRenderer livingRenderer) {
                livingRenderer.addLayer(new ShimmerweaveVisorLayer(livingRenderer, modelSet));
            }
        }
        for (EntityType<?> type : event.getEntityTypes()) {
            EntityRenderer<?> renderer = event.getRenderer(type);
            if (renderer instanceof LivingEntityRenderer livingRenderer
                    && livingRenderer.getModel() instanceof HumanoidModel) {
                livingRenderer.addLayer(new ShimmerweaveVisorLayer(livingRenderer, modelSet));
            }
        }
    }

    /**
     * Register model layer definitions.
     */
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AuroralNautilusModel.LAYER_LOCATION, AuroralNautilusModel::createBodyLayer);
        event.registerLayerDefinition(AuroralNautilusArmorModel.LAYER_LOCATION, AuroralNautilusArmorModel::createBodyLayer);
    }

    /**
     * Register entity renderers.
     */
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.AURORAL_NAUTILUS.get(), AuroralNautilusRenderer::new);
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

    /**
     * Reset client state when disconnecting from server.
     * Clears cached state.
     */
    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientAuroraState.reset();
        AuroraSkyRenderer.reset();
        SnowFootprintManager.clear();
        AuroraMusicHandler.forceStop();
        SnowFootprintRenderer.dispose();
    }
}
