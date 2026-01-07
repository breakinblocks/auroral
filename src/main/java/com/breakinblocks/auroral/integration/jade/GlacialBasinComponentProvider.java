package com.breakinblocks.auroral.integration.jade;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.block.GlacialBasinBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Client-side component provider for Glacial Basin.
 * Renders tooltip with aura level and aurora status.
 */
public enum GlacialBasinComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final Identifier UID = Auroral.id("glacial_basin");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        int auraLevel = accessor.getBlockState().getValue(GlacialBasinBlock.AURA_LEVEL);
        int maxLevel = GlacialBasinBlock.MAX_AURA_LEVEL;

        Component auraText = Component.translatable("block.auroral.glacial_basin.aura_level", auraLevel, maxLevel);
        if (auraLevel == maxLevel) {
            tooltip.add(auraText.copy().withStyle(ChatFormatting.AQUA));
        } else if (auraLevel > 0) {
            tooltip.add(auraText.copy().withStyle(ChatFormatting.BLUE));
        } else {
            tooltip.add(auraText.copy().withStyle(ChatFormatting.GRAY));
        }

        CompoundTag data = accessor.getServerData();
        if (data.contains("aurora_active")) {
            boolean auroraActive = data.getBoolean("aurora_active").orElse(false);
            if (auroraActive) {
                tooltip.add(Component.translatable("block.auroral.glacial_basin.aurora_active")
                    .withStyle(ChatFormatting.GREEN));
            } else if (auraLevel < maxLevel) {
                tooltip.add(Component.translatable("block.auroral.glacial_basin.aurora_inactive")
                    .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
