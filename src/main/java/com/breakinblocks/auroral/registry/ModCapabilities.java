package com.breakinblocks.auroral.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

public final class ModCapabilities {

    private ModCapabilities() {}

    public static void register(IEventBus eventBus) {
        eventBus.addListener(ModCapabilities::onRegister);
    }

    private static void onRegister(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.GLACIAL_BASIN.get(),
            (be, side) -> be.getItemHandler()
        );
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.COLD_BREWING_STAND.get(),
            (be, side) -> side == null ? new InvWrapper(be) : new SidedInvWrapper(be, side)
        );
    }
}
