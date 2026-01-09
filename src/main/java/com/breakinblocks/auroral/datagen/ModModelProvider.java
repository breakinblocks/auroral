package com.breakinblocks.auroral.datagen;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Data generator for item models.
 * Uses NeoForge's ItemModelProvider for 1.21.1 compatibility.
 *
 * Complex blocks with property-based variants use static JSON files in
 * src/main/resources/assets/auroral/ as they require manual configuration.
 */
public class ModModelProvider extends ItemModelProvider {

    public ModModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Auroral.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // === ITEMS - using basicItem for flat items ===

        // Basic Materials - flat items
        basicItem(ModItems.UNREFINED_SHIMMERSTEEL.get());
        basicItem(ModItems.SHIMMERSTEEL_INGOT.get());
        basicItem(ModItems.SHIMMERWEAVE_FABRIC.get());
        basicItem(ModItems.WOVEN_LEATHER.get());
        basicItem(ModItems.AURORA_SHARD.get());
        basicItem(ModItems.FROZEN_PETALS.get());

        // Food items
        basicItem(ModItems.GLOW_LEEK.get());
        basicItem(ModItems.CANDIED_GLOW_LEEK.get());
        basicItem(ModItems.HOT_COCOA.get());
        basicItem(ModItems.FROSTED_COOKIES.get());

        // Seeds
        basicItem(ModItems.GLOW_LEEK_SEEDS.get());

        // Tools - handheld items
        handheldItem(ModItems.SHIMMERSTEEL_PICKAXE.get());
        handheldItem(ModItems.SHIMMERSTEEL_AXE.get());
        handheldItem(ModItems.SHIMMERSTEEL_SHOVEL.get());
        handheldItem(ModItems.SHIMMERSTEEL_HOE.get());
        handheldItem(ModItems.SHIMMERSTEEL_SWORD.get());
        // Bow uses static JSON with minecraft:item/bow parent and overrides for pulling animation

        // Armor - flat items
        basicItem(ModItems.SHIMMERWEAVE_GOGGLES.get());
        basicItem(ModItems.SHIMMERWEAVE_TUNIC.get());
        basicItem(ModItems.SHIMMERWEAVE_LEGGINGS.get());
        basicItem(ModItems.SHIMMERWEAVE_SKATES.get());

        // Smithing template
        basicItem(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get());

        // Spawn eggs - use parent template_spawn_egg
        withExistingParent(ModItems.AURORAL_NAUTILUS_SPAWN_EGG.getId().getPath(),
            ResourceLocation.withDefaultNamespace("item/template_spawn_egg"));
        withExistingParent(ModItems.AURORAL_SNOWLETTE_SPAWN_EGG.getId().getPath(),
            ResourceLocation.withDefaultNamespace("item/template_spawn_egg"));

        // Block items - Aurora Bloom and Snow Angel use flat item models with block textures
        withExistingParent(ModItems.AURORA_BLOOM_ITEM.getId().getPath(), ResourceLocation.withDefaultNamespace("item/generated"))
            .texture("layer0", Auroral.id("block/aurora_bloom"));
        withExistingParent(ModItems.SNOW_ANGEL_ITEM.getId().getPath(), ResourceLocation.withDefaultNamespace("item/generated"))
            .texture("layer0", Auroral.id("block/snow_angel"));

        // Note: Complex blocks with property variants (glacial_basin, hearthwood_log,
        // aurora_lantern, glow_leek, cold_brewing_stand) use static JSON files.
    }

    /**
     * Helper for handheld items that aren't covered by the parent handheldItem method.
     */
    private void simpleHandheldItem(net.minecraft.world.item.Item item) {
        ResourceLocation id = item.builtInRegistryHolder().key().location();
        withExistingParent(id.getPath(), ResourceLocation.withDefaultNamespace("item/handheld"))
            .texture("layer0", Auroral.id("item/" + id.getPath()));
    }
}
