package com.breakinblocks.auroral.integration.jade;

import com.breakinblocks.auroral.block.GlacialBasinBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Jade server data provider for Glacial Basin.
 * Sends aura level data from server to client for tooltip display.
 */
public enum GlacialBasinDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof GlacialBasinBlockEntity basin) {
            data.putInt("AuraLevel", basin.getAuraLevel());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return AuroralJadePlugin.GLACIAL_BASIN;
    }
}
