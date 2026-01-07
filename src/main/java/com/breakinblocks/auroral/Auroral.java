package com.breakinblocks.auroral;

import com.mojang.logging.LogUtils;
import com.breakinblocks.auroral.client.AuroralClient;
import com.breakinblocks.auroral.config.AuroralConfig;
import com.breakinblocks.auroral.registry.ModBlockEntities;
import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.registry.ModCreativeTabs;
import com.breakinblocks.auroral.registry.ModDataAttachments;
import com.breakinblocks.auroral.registry.ModEffects;
import com.breakinblocks.auroral.registry.ModEntities;
import com.breakinblocks.auroral.registry.ModItems;
import com.breakinblocks.auroral.registry.ModMenuTypes;
import com.breakinblocks.auroral.registry.ModParticles;
import com.breakinblocks.auroral.registry.ModSounds;
import com.breakinblocks.auroral.events.EntityEventHandler;
import com.breakinblocks.auroral.events.ItemEventHandler;
import com.breakinblocks.auroral.integration.guideme.AuroralGuide;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Auroral.MOD_ID)
public class Auroral {
    public static final String MOD_ID = "auroral";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public Auroral(IEventBus eventBus, ModContainer container, Dist dist) {
        LOGGER.info("Auroral initializing...");

        // Register all deferred registers
        ModBlocks.BLOCKS.register(eventBus);
        ModItems.ITEMS.register(eventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(eventBus);
        ModEntities.ENTITIES.register(eventBus);
        ModEffects.MOB_EFFECTS.register(eventBus);
        ModDataAttachments.ATTACHMENT_TYPES.register(eventBus);
        ModCreativeTabs.CREATIVE_TABS.register(eventBus);
        ModSounds.SOUNDS.register(eventBus);
        ModParticles.PARTICLES.register(eventBus);
        ModMenuTypes.MENU_TYPES.register(eventBus);

        // Register entity event handlers (for attributes)
        EntityEventHandler.register(eventBus);

        // Register item event handlers (for modifying vanilla items)
        ItemEventHandler.register(eventBus);

        // Register configs
        container.registerConfig(ModConfig.Type.STARTUP, AuroralConfig.startupSpec);
        container.registerConfig(ModConfig.Type.SERVER, AuroralConfig.serverSpec);
        container.registerConfig(ModConfig.Type.CLIENT, AuroralConfig.clientSpec);

        // Client-specific setup
        if (dist.isClient()) {
            AuroralClient.init(eventBus);
        }

        // Optional mod integrations
        if (ModList.get().isLoaded("guideme")) {
            AuroralGuide.init();
        }

        LOGGER.info("Auroral initialized successfully!");
    }
}
