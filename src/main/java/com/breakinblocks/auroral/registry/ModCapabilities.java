package com.breakinblocks.auroral.registry;

import com.breakinblocks.auroral.block.GlacialBasinBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

public final class ModCapabilities {

    private ModCapabilities() {}

    public static void register(IEventBus eventBus) {
        eventBus.addListener(ModCapabilities::onRegister);
    }

    private static void onRegister(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.Item.BLOCK,
            ModBlockEntities.GLACIAL_BASIN.get(),
            (be, side) -> be.getItemHandler()
        );
        event.registerBlockEntity(
            Capabilities.Item.BLOCK,
            ModBlockEntities.COLD_BREWING_STAND.get(),
            WorldlyContainerWrapper::new
        );
    }
}
