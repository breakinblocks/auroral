package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.config.AuroralConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/**
 * Handles item-related modifications, including modifying vanilla item properties.
 */
public class ItemEventHandler {

    /**
     * Register this handler on the mod event bus.
     *
     * @param eventBus The mod event bus
     */
    public static void register(IEventBus eventBus) {
        eventBus.addListener(ItemEventHandler::onModifyDefaultComponents);
    }

    /**
     * Modify default item components to apply config-driven changes.
     * Uses STARTUP config which is loaded early enough for this event.
     */
    private static void onModifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        int maxStackSize = AuroralConfig.STARTUP.snowballMaxStackSize.get();

        // Only modify if different from vanilla default (16)
        if (maxStackSize != 16) {
            event.modify(Items.SNOWBALL, builder -> {
                builder.set(DataComponents.MAX_STACK_SIZE, maxStackSize);
            });
            Auroral.LOGGER.debug("Modified snowball max stack size to {}", maxStackSize);
        }
    }
}
