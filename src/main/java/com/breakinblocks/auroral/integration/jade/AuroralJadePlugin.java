package com.breakinblocks.auroral.integration.jade;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.block.GlacialBasinBlock;
import com.breakinblocks.auroral.block.GlacialBasinBlockEntity;
import com.breakinblocks.auroral.block.HearthwoodLogBlock;
import com.breakinblocks.auroral.block.HearthwoodLogBlockEntity;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade plugin for Auroral mod.
 * Provides tooltip information for Glacial Basin and Hearthwood Log blocks.
 */
@WailaPlugin
public class AuroralJadePlugin implements IWailaPlugin {

    public static final ResourceLocation GLACIAL_BASIN = Auroral.id("glacial_basin");
    public static final ResourceLocation HEARTHWOOD_LOG = Auroral.id("hearthwood_log");

    @Override
    public void register(IWailaCommonRegistration registration) {
        // Register server data providers
        registration.registerBlockDataProvider(GlacialBasinDataProvider.INSTANCE, GlacialBasinBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Register client tooltip providers
        registration.registerBlockComponent(GlacialBasinComponentProvider.INSTANCE, GlacialBasinBlock.class);
        registration.registerBlockComponent(HearthwoodLogComponentProvider.INSTANCE, HearthwoodLogBlock.class);
    }
}
