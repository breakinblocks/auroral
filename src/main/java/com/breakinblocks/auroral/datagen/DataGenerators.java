package com.breakinblocks.auroral.datagen;

import com.breakinblocks.auroral.Auroral;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Main data generation entry point.
 * Registers all data providers for recipes, loot tables, tags, models, and language files.
 *
 * NeoForge 21.11 uses GatherDataEvent.Client for client data generation.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Block states and models (combined ModelProvider)
        event.createProvider(ModModelProvider::new);

        // Language files
        event.createProvider(ModLanguageProvider::new);

        // Recipes
        event.createProvider((output) -> new ModRecipeProvider.Runner(output, lookupProvider));

        // Loot tables
        event.createProvider((output) -> new ModLootTableProvider(output, lookupProvider));

        // Block tags
        event.createProvider(ModBlockTagsProvider::new);

        // Item tags
        event.createProvider(ModItemTagsProvider::new);

        // Note: Advancements use JSON files in data/auroral/advancement/
        // The datagen API changed significantly in 1.21.11
    }
}
