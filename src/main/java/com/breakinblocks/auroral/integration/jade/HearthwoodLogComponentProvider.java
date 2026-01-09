package com.breakinblocks.auroral.integration.jade;

import com.breakinblocks.auroral.block.HearthwoodLogBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade tooltip provider for Hearthwood Log.
 * Shows whether the log is lit and provides warmth.
 */
public enum HearthwoodLogComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        boolean lit = accessor.getBlockState().getValue(HearthwoodLogBlock.LIT);

        if (lit) {
            tooltip.add(Component.translatable("tooltip.auroral.hearthwood_log.burning"));
        } else {
            tooltip.add(Component.translatable("tooltip.auroral.hearthwood_log.unlit"));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return AuroralJadePlugin.HEARTHWOOD_LOG;
    }
}
