package com.breakinblocks.auroral.integration.jade;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.block.GlacialBasinBlock;
import com.breakinblocks.auroral.block.GlacialBasinBlockEntity;
import net.minecraft.resources.Identifier;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade (Waila) plugin for Auroral.
 * Provides tooltip information for the Glacial Basin block.
 */
@WailaPlugin
public class AuroralJadePlugin implements IWailaPlugin {

    public static final Identifier GLACIAL_BASIN = Auroral.id("glacial_basin");

    @Override
    public void register(IWailaCommonRegistration registration) {
        // Register data provider (server-side) - must be separate from component provider since 1.21.6
        registration.registerBlockDataProvider(GlacialBasinDataProvider.INSTANCE, GlacialBasinBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Register component providers for client-side rendering
        registration.registerBlockComponent(GlacialBasinComponentProvider.INSTANCE, GlacialBasinBlock.class);
    }
}
