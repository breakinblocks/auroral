package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.entity.AuroralNautilusEntity;
import com.breakinblocks.auroral.entity.AuroralSnowletteEntity;
import com.breakinblocks.auroral.registry.ModEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * Handles entity-related events such as attribute registration.
 * This class registers events on the MOD bus through manual registration.
 */
public class EntityEventHandler {

    /**
     * Register entity event handlers on the mod event bus.
     * Call this from the main mod constructor.
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(EntityEventHandler::onEntityAttributeCreation);
    }

    /**
     * Register entity attributes for custom mobs.
     */
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.AURORAL_NAUTILUS.get(), AuroralNautilusEntity.createAttributes().build());
        event.put(ModEntities.AURORAL_SNOWLETTE.get(), AuroralSnowletteEntity.createAttributes().build());
    }
}
