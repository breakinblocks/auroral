package com.breakinblocks.auroral.datagen;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModItems;
import com.breakinblocks.auroral.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Data generator for item tags.
 * NeoForge 21.11 uses simplified ItemTagsProvider from NeoForge.
 */
public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Auroral.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Add shimmersteel ingot to common ingots tag
        tag(Tags.Items.INGOTS)
            .add(ModItems.SHIMMERSTEEL_INGOT.get());

        // Aurora self-repair tag - items that repair during aurora events
        tag(ModTags.Items.AURORA_SELF_REPAIR)
            // Shimmersteel tools
            .add(ModItems.SHIMMERSTEEL_PICKAXE.get())
            .add(ModItems.SHIMMERSTEEL_AXE.get())
            .add(ModItems.SHIMMERSTEEL_SHOVEL.get())
            .add(ModItems.SHIMMERSTEEL_SWORD.get())
            .add(ModItems.SHIMMERSTEEL_HOE.get())
            .add(ModItems.SHIMMERSTEEL_BOW.get())
            .add(ModItems.SHIMMER_SPEAR.get())
            // Shimmerweave armor
            .add(ModItems.SHIMMERWEAVE_GOGGLES.get())
            .add(ModItems.SHIMMERWEAVE_TUNIC.get())
            .add(ModItems.SHIMMERWEAVE_LEGGINGS.get())
            .add(ModItems.SHIMMERWEAVE_SKATES.get());
    }
}
