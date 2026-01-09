package com.breakinblocks.auroral.integration.jade;

import com.breakinblocks.auroral.block.GlacialBasinBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade tooltip provider for Glacial Basin.
 * Shows the current aura level in the tooltip.
 */
public enum GlacialBasinComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        // Get aura level from block state
        int auraLevel = accessor.getBlockState().getValue(GlacialBasinBlock.AURA_LEVEL);

        // Also check server data for more accurate info
        CompoundTag serverData = accessor.getServerData();
        if (serverData.contains("AuraLevel")) {
            auraLevel = serverData.getInt("AuraLevel");
        }

        if (auraLevel > 0) {
            tooltip.add(Component.translatable("tooltip.auroral.glacial_basin.aura_level", auraLevel, GlacialBasinBlock.MAX_AURA_LEVEL));
        } else {
            tooltip.add(Component.translatable("tooltip.auroral.glacial_basin.empty"));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return AuroralJadePlugin.GLACIAL_BASIN;
    }
}
