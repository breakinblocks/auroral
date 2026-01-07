package com.breakinblocks.auroral.datagen;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

/**
 * Data generator for block states, block models, and item models.
 * NeoForge 21.11 uses the vanilla ModelProvider with NeoForge extensions.
 *
 * Complex blocks with property-based variants use static JSON files in
 * src/main/resources/assets/auroral/ as the 1.21.11 API differs significantly
 * from earlier versions and documentation is limited.
 */
public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, Auroral.MOD_ID);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        // Return empty - we manually handle blocks or use static JSON
        return Stream.empty();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        // Return empty - we manually handle items or use static JSON
        return Stream.empty();
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // === SIMPLE BLOCKS ===
        // Shimmering Ice - simple translucent cube
        blockModels.createTrivialCube(ModBlocks.SHIMMERING_ICE.get());

        // === ITEMS - using generateFlatItem ===

        // Basic Materials - flat items
        itemModels.generateFlatItem(ModItems.UNREFINED_SHIMMERSTEEL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SHIMMERSTEEL_INGOT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SHIMMERWEAVE_FABRIC.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.WOVEN_LEATHER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.AURORA_SHARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FROZEN_PETALS.get(), ModelTemplates.FLAT_ITEM);

        // Food items
        itemModels.generateFlatItem(ModItems.GLOW_LEEK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.CANDIED_GLOW_LEEK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.HOT_COCOA.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FROSTED_COOKIES.get(), ModelTemplates.FLAT_ITEM);

        // Seeds
        itemModels.generateFlatItem(ModItems.GLOW_LEEK_SEEDS.get(), ModelTemplates.FLAT_ITEM);

        // Tools - handheld items
        itemModels.generateFlatItem(ModItems.SHIMMERSTEEL_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.SHIMMERSTEEL_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.SHIMMERSTEEL_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.SHIMMERSTEEL_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.SHIMMERSTEEL_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.SHIMMER_SPEAR.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        // Bow uses static JSON with minecraft:item/bow parent and overrides for pulling animation

        // Armor - flat items
        itemModels.generateFlatItem(ModItems.SHIMMERWEAVE_GOGGLES.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SHIMMERWEAVE_TUNIC.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SHIMMERWEAVE_LEGGINGS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SHIMMERWEAVE_SKATES.get(), ModelTemplates.FLAT_ITEM);

        // Smithing template
        itemModels.generateFlatItem(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get(), ModelTemplates.FLAT_ITEM);

        // Spawn eggs use static JSON files with minecraft:item/template_spawn_egg parent

        // Block items with flat models
        blockModels.registerSimpleFlatItemModel(ModBlocks.AURORA_BLOOM.get());
        blockModels.registerSimpleFlatItemModel(ModBlocks.SNOW_ANGEL.get());

        // Note: Complex blocks with property variants (glacial_basin, hearthwood_log,
        // aurora_lantern, aurora_bloom, glow_leek, cold_brewing_stand) use static JSON
        // files due to significant API changes in 1.21.11 that differ from documentation.
    }
}
