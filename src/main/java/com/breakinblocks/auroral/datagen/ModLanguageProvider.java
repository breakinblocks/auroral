package com.breakinblocks.auroral.datagen;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Data generator for language files (translations).
 */
public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, Auroral.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // Creative tab
        add("itemGroup." + Auroral.MOD_ID, "Auroral");

        // Blocks
        addBlock(ModBlocks.GLACIAL_BASIN, "Glacial Basin");
        addBlock(ModBlocks.COLD_BREWING_STAND, "Cold Brewing Stand");
        addBlock(ModBlocks.HEARTHWOOD_LOG, "Hearthwood Log");
        addBlock(ModBlocks.SHIMMERING_ICE, "Shimmering Ice");
        addBlock(ModBlocks.AURORA_BLOOM, "Aurora Bloom");
        addBlock(ModBlocks.GLOW_LEEK, "Glow-Leek");
        addBlock(ModBlocks.AURORA_LANTERN, "Aurora Lantern");
        addBlock(ModBlocks.SNOW_ANGEL, "Snow Angel");
        addBlock(ModBlocks.SHIMMER_SOIL, "Shimmer Soil");

        // Items - Materials
        addItem(ModItems.UNREFINED_SHIMMERSTEEL, "Unrefined Shimmersteel");
        addItem(ModItems.SHIMMERSTEEL_INGOT, "Shimmersteel Ingot");
        addItem(ModItems.SHIMMERWEAVE_FABRIC, "Shimmerweave Fabric");
        addItem(ModItems.WOVEN_LEATHER, "Woven Leather");
        addItem(ModItems.AURORA_SHARD, "Aurora Shard");
        addItem(ModItems.FROZEN_PETALS, "Frozen Petals");

        // Items - Shimmersteel Tools
        addItem(ModItems.SHIMMERSTEEL_PICKAXE, "Shimmersteel Pickaxe");
        addItem(ModItems.SHIMMERSTEEL_AXE, "Shimmersteel Axe");
        addItem(ModItems.SHIMMERSTEEL_SHOVEL, "Shimmersteel Shovel");
        addItem(ModItems.SHIMMERSTEEL_HOE, "Shimmersteel Hoe");
        addItem(ModItems.SHIMMERSTEEL_SWORD, "Shimmersteel Sword");
        addItem(ModItems.SHIMMERSTEEL_BOW, "Shimmersteel Bow");

        // Items - Shimmerweave Armor
        addItem(ModItems.SHIMMERWEAVE_GOGGLES, "Shimmerweave Goggles");
        addItem(ModItems.SHIMMERWEAVE_TUNIC, "Shimmerweave Tunic");
        addItem(ModItems.SHIMMERWEAVE_LEGGINGS, "Shimmerweave Leggings");
        addItem(ModItems.SHIMMERWEAVE_SKATES, "Shimmerweave Skates");

        // Items - Other
        addItem(ModItems.GLOW_LEEK, "Glow-Leek");
        // Note: GLOW_LEEK_SEEDS is a BlockItem that uses the block's translation key
        addItem(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE, "Smithing Template");

        // Items - Food
        addItem(ModItems.CANDIED_GLOW_LEEK, "Candied Glow-Leek");
        addItem(ModItems.HOT_COCOA, "Hot Cocoa");
        addItem(ModItems.FROSTED_COOKIES, "Frosted Cookies");

        // Spawn Eggs
        addItem(ModItems.AURORAL_NAUTILUS_SPAWN_EGG, "Auroral Nautilus Spawn Egg");
        addItem(ModItems.AURORAL_SNOWLETTE_SPAWN_EGG, "Auroral Snowlette Spawn Egg");

        // GuideME Guide
        add("item.auroral.guide", "The Auroral Guidebook");

        // Tooltips and messages
        add("block.auroral.glacial_basin.aura_level", "Aura Level: %d/%d");
        add("block.auroral.glacial_basin.infusion_hint", "Right-click with items during Aurora to infuse");
        add("block.auroral.glacial_basin.aurora_active", "Aurora Active - Collecting Aura");
        add("block.auroral.glacial_basin.aurora_inactive", "Waiting for Aurora...");
        add("block.auroral.glacial_basin.not_enough_aura", "Requires %d aura levels");

        // JEI integration
        add("auroral.jei.category.basin_infusion", "Basin Infusion");
        add("auroral.jei.basin_infusion.tooltip", "Requires Liquid Aura (collected during Aurora)");

        // Config
        add("auroral.config.aurora_chance", "Aurora Chance");
        add("auroral.config.aurora_repair_rate", "Aurora Repair Rate");

        // Effects
        add("effect.auroral.frostbite", "Frostbite");
        add("effect.auroral.frostbite_immunity", "Frostbite Immunity");

        // Entities
        add("entity.auroral.auroral_nautilus", "Auroral Nautilus");
        add("entity.auroral.auroral_snowlette", "Auroral Snowlette");
        add("entity.auroral.star_shot", "Star-Shot");

        // Smithing Template descriptions
        add("upgrade.auroral.shimmersteel_upgrade", "Shimmersteel Upgrade");
        add("item.auroral.smithing_template.shimmersteel_upgrade.applies_to", "Iron Equipment");
        add("item.auroral.smithing_template.shimmersteel_upgrade.ingredients", "Shimmersteel Ingot or Shimmerweave Fabric");
        add("item.auroral.smithing_template.shimmersteel_upgrade.base_slot_description", "Add iron armor, weapon, or tool");
        add("item.auroral.smithing_template.shimmersteel_upgrade.additions_slot_description", "Add Shimmersteel Ingot or Shimmerweave Fabric");

        // Armor Trim Materials
        add("trim_material.auroral.shimmersteel", "Shimmersteel Material");

        // Advancements
        add("advancement.auroral.root.title", "Auroral");
        add("advancement.auroral.root.description", "Enter a world touched by the aurora");
        add("advancement.auroral.aurora_shard.title", "Celestial Gift");
        add("advancement.auroral.aurora_shard.description", "Collect an Aurora Shard");
        add("advancement.auroral.glacial_basin.title", "Frozen Forge");
        add("advancement.auroral.glacial_basin.description", "Craft a Glacial Basin to harness aurora energy");
        add("advancement.auroral.shimmersteel_ingot.title", "Celestial Metal");
        add("advancement.auroral.shimmersteel_ingot.description", "Create a Shimmersteel Ingot through aurora infusion");
        add("advancement.auroral.shimmerweave_fabric.title", "Woven Light");
        add("advancement.auroral.shimmerweave_fabric.description", "Create Shimmerweave Fabric through aurora infusion");
        add("advancement.auroral.shimmersteel_tools.title", "Ice Armory");
        add("advancement.auroral.shimmersteel_tools.description", "Craft all Shimmersteel tools");
        add("advancement.auroral.shimmerweave_armor.title", "Aurora Clad");
        add("advancement.auroral.shimmerweave_armor.description", "Craft a full set of Shimmerweave armor");
        add("advancement.auroral.cold_brewing_stand.title", "Cold Alchemy");
        add("advancement.auroral.cold_brewing_stand.description", "Craft a Cold Brewing Stand");
        add("advancement.auroral.glow_leek.title", "Night Vision");
        add("advancement.auroral.glow_leek.description", "Harvest a Glow-Leek");
        add("advancement.auroral.hearthwood_log.title", "Hearth and Home");
        add("advancement.auroral.hearthwood_log.description", "Obtain a Hearthwood Log");
        add("advancement.auroral.auroral_snowlette.title", "Frozen Companion");
        add("advancement.auroral.auroral_snowlette.description", "Tame an Auroral Snowlette");
        add("advancement.auroral.ride_nautilus.title", "Sky Sailor");
        add("advancement.auroral.ride_nautilus.description", "Ride an Auroral Nautilus");
        add("advancement.auroral.aurora_lantern.title", "Guiding Light");
        add("advancement.auroral.aurora_lantern.description", "Craft an Aurora Lantern");
        add("advancement.auroral.warden_slayer.title", "Light in the Darkness");
        add("advancement.auroral.warden_slayer.description", "Slay a Warden while holding an Aurora Lantern");

        // Jade integration
        add("config.jade.plugin_auroral.glacial_basin", "Glacial Basin");
        add("config.jade.plugin_auroral.hearthwood_log", "Hearthwood Log");

        // Sound subtitles
        add("subtitles.auroral.aurora_music", "Aurora music plays");
        add("subtitles.auroral.aurora_ambient", "Aurora hums");
        add("subtitles.auroral.aurora_start", "Aurora begins");
        add("subtitles.auroral.aurora_end", "Aurora fades");
        add("subtitles.auroral.basin_infuse", "Basin infuses");
        add("subtitles.auroral.basin_fill", "Basin fills with aura");
        add("subtitles.auroral.star_shot_fire", "Star-Shot fires");
        add("subtitles.auroral.star_shot_impact", "Star-Shot explodes");
        add("subtitles.auroral.nautilus_ambient", "Auroral Nautilus chimes");
        add("subtitles.auroral.nautilus_spawn", "Auroral Nautilus appears");
        add("subtitles.auroral.nautilus_death", "Auroral Nautilus fades");
        add("subtitles.auroral.hearthwood_log_crackle", "Hearthwood Log crackles");
        add("subtitles.auroral.hearthwood_log_ignite", "Hearthwood Log ignites");
        add("subtitles.auroral.skates_slide", "Skates slide");
    }
}
