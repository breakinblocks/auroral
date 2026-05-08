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
        addBlock(ModBlocks.AURORA_BLOOM_DECORATIVE, "Aurora Bloom");
        addBlock(ModBlocks.ENDER_BLOOM, "Ender Bloom");
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
        addItem(ModItems.AURORA_ENDER_SHARD, "Aurora Ender Shard");

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
        addItem(ModItems.ROASTED_SNOWBALL, "Roasted Snowball");
        addItem(ModItems.SUGARED_ROASTED_SNOWBALL, "Sugared Roasted Snowball");
        addItem(ModItems.SNORE, "S'nore");

        // Roasted snowball flavor messages
        add("item.auroral.roasted_snowball.eaten", "The Magestic Nautili will no longer Soar In your skies");
        add("item.auroral.sugared_roasted_snowball.eaten", "The Majestic Nautili fly once again!");
        add("item.auroral.roasted_snowball.tooltip.line1", "A toasted snowball. Delicious, lightly filling.");
        add("item.auroral.roasted_snowball.tooltip.line2", "Eating one offends the Auroral Nautili. Wild Nautili will no longer spawn near you.");
        add("item.auroral.sugared_roasted_snowball.tooltip.line1", "A Roasted Snowball rolled in sugar. Light snack, shiny coating.");
        add("item.auroral.sugared_roasted_snowball.tooltip.line2", "Eating one apologizes to the Auroral Nautili so they grace your skies again.");

        // Equipment and block tooltips (ability lines in blue, info lines in gray)
        add("tooltip.auroral.shimmersteel_pickaxe.gem_fortune", "Fortune III on gem ores");
        add("tooltip.auroral.shimmersteel_axe.force_oxidize", "Right-click copper to force oxidation");
        add("tooltip.auroral.shimmersteel_shovel.silk_touch", "Inherent Silk Touch");
        add("tooltip.auroral.shimmersteel_hoe.silk_touch", "Inherent Silk Touch");
        add("tooltip.auroral.shimmersteel_hoe.shimmer_soil", "Tills snow into Shimmer Soil");
        add("tooltip.auroral.shimmersteel_sword.frostbite", "Applies Frostbite on hit");
        add("tooltip.auroral.shimmersteel_sword.execute", "Executes enemies below 15% health");
        add("tooltip.auroral.shimmersteel_bow.star_shot", "Fires Star-Shots from snowballs");

        add("tooltip.auroral.shimmerweave_goggles.glow_hostiles", "Highlights hostile mobs nearby");
        add("tooltip.auroral.shimmerweave_tunic.extinguish", "Auto-extinguishes fire");
        add("tooltip.auroral.shimmerweave_leggings.snow_speed", "Speed boost on snow");
        add("tooltip.auroral.shimmerweave_leggings.soul_speed", "Speed boost on Soul Sand and Soul Soil");
        add("tooltip.auroral.shimmerweave_skates.ice_speed", "Ice skating speed on ice and packed ice");
        add("tooltip.auroral.shimmerweave_skates.frost_walker", "Freezes water underfoot");
        add("tooltip.auroral.shimmerweave_skates.lava_to_obsidian", "Turns surface lava into obsidian");
        add("tooltip.auroral.shimmerweave_skates.no_ice_fall", "No fall damage on ice or obsidian");

        add("tooltip.auroral.hearthwood_log.burn_time", "Burns for 7 in-game days while loaded");
        add("tooltip.auroral.hearthwood_log.frostbite_immunity", "Grants Frostbite Immunity within 16 blocks");
        add("tooltip.auroral.hearthwood_log.phantom_ward", "Ignites phantoms; wards off targeting");
        add("tooltip.auroral.hearthwood_log.aurora_catalyst", "Aurora Catalyst: +15% per log, up to +45%");

        add("tooltip.auroral.aurora_lantern.decorative", "Decorative light with aurora sparkle");

        add("tooltip.auroral.shimmering_ice.never_melts", "Never melts");
        add("tooltip.auroral.shimmering_ice.hydrates", "Hydrates adjacent farmland");
        add("tooltip.auroral.shimmering_ice.glow_leek", "Supports Glow-Leek crops");

        add("tooltip.auroral.glacial_basin.collect_aura", "Collects Liquid Aura under open sky during auroras");
        add("tooltip.auroral.glacial_basin.infuse", "Right-click with items during auroras to infuse");

        add("tooltip.auroral.cold_brewing_stand.no_nether", "Brews potions without a Blaze Rod");

        add("tooltip.auroral.snow_angel.imprint", "Imprinted by sneak-right-clicking on snow");
        add("tooltip.auroral.snow_angel.fades", "Fades after 5 minutes; preserve with Frozen Petals");

        add("tooltip.auroral.shimmer_soil.growth_boost", "3x growth at night, 5x during auroras");
        add("tooltip.auroral.shimmer_soil.tilled", "Till snow with a Shimmersteel Hoe");

        add("tooltip.auroral.aurora_bloom.grows_at_night", "Grows at night on snow; withers in daylight");
        add("tooltip.auroral.aurora_bloom.frozen_petals", "Mature blooms drop Frozen Petals");

        add("tooltip.auroral.ender_bloom.ender_shards", "Mature plants drop Aurora Ender Shards");

        add("tooltip.auroral.glow_leek.grows_on_ice", "Grows on Shimmering Ice");
        add("tooltip.auroral.glow_leek.night_vision", "Eating grants Night Vision and Glowing");

        add("tooltip.auroral.frozen_petals.preserve_snow_angel", "Right-click a Snow Angel to preserve it");
        add("tooltip.auroral.aurora_ender_shard.ender_bloom", "Right-click an Aurora Bloom to transform it");

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
