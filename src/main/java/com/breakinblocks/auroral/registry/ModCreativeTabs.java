package com.breakinblocks.auroral.registry;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.integration.guideme.AuroralGuide;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Auroral.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AURORABOUND_TAB =
        CREATIVE_TABS.register("auroral", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.auroral"))
            .icon(() -> new ItemStack(ModItems.AURORA_SHARD.get()))
            .displayItems((parameters, output) -> {
                // Guidebook (if GuideME is loaded)
                if (ModList.get().isLoaded("guideme")) {
                    try {
                        ItemStack guideItem = AuroralGuide.createGuideItem();
                        if (guideItem != null && !guideItem.isEmpty()) {
                            output.accept(guideItem);
                        }
                    } catch (Exception e) {
                        Auroral.LOGGER.debug("Could not add GuideME guidebook to creative tab: {}", e.getMessage());
                    }
                }

                // Blocks
                output.accept(ModBlocks.GLACIAL_BASIN.get());
                output.accept(ModBlocks.COLD_BREWING_STAND.get());
                output.accept(ModBlocks.HEARTHWOOD_LOG.get());
                output.accept(ModBlocks.SHIMMERING_ICE.get());
                output.accept(ModBlocks.SHIMMER_SOIL.get());
                output.accept(ModBlocks.AURORA_BLOOM.get());
                output.accept(ModBlocks.AURORA_LANTERN.get());
                output.accept(ModBlocks.SNOW_ANGEL.get());

                // Materials
                output.accept(ModItems.AURORA_SHARD.get());
                output.accept(ModItems.UNREFINED_SHIMMERSTEEL.get());
                output.accept(ModItems.SHIMMERSTEEL_INGOT.get());
                output.accept(ModItems.SHIMMERWEAVE_FABRIC.get());
                output.accept(ModItems.WOVEN_LEATHER.get());
                output.accept(ModItems.FROZEN_PETALS.get());

                // Food & Crops
                output.accept(ModItems.GLOW_LEEK.get());
                output.accept(ModItems.GLOW_LEEK_SEEDS.get());
                output.accept(ModItems.CANDIED_GLOW_LEEK.get());
                output.accept(ModItems.HOT_COCOA.get());
                output.accept(ModItems.FROSTED_COOKIES.get());

                // Shimmersteel Tools
                output.accept(ModItems.SHIMMERSTEEL_PICKAXE.get());
                output.accept(ModItems.SHIMMERSTEEL_AXE.get());
                output.accept(ModItems.SHIMMERSTEEL_SHOVEL.get());
                output.accept(ModItems.SHIMMERSTEEL_HOE.get());
                output.accept(ModItems.SHIMMERSTEEL_SWORD.get());
                output.accept(ModItems.SHIMMERSTEEL_BOW.get());

                // Smithing Templates
                output.accept(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get());

                // Shimmerweave Armor
                output.accept(ModItems.SHIMMERWEAVE_GOGGLES.get());
                output.accept(ModItems.SHIMMERWEAVE_TUNIC.get());
                output.accept(ModItems.SHIMMERWEAVE_LEGGINGS.get());
                output.accept(ModItems.SHIMMERWEAVE_SKATES.get());

                // Spawn Eggs
                output.accept(ModItems.AURORAL_NAUTILUS_SPAWN_EGG.get());
                output.accept(ModItems.AURORAL_SNOWLETTE_SPAWN_EGG.get());
            })
            .build()
        );
}
