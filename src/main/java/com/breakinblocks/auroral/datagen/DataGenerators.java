package com.breakinblocks.auroral.datagen;

import com.breakinblocks.auroral.Auroral;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Main data generation entry point.
 * Registers all data providers for recipes, loot tables, tags, models, and language files.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Block states and models
        generator.addProvider(event.includeClient(), new ModModelProvider(packOutput, existingFileHelper));

        // Language files
        generator.addProvider(event.includeClient(), new ModLanguageProvider(packOutput));

        // Recipes
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));

        // Loot tables
        generator.addProvider(event.includeServer(), new ModLootTableProvider(packOutput, lookupProvider));

        // Block tags
        ModBlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);

        // Item tags (depends on block tags)
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
    }
}
