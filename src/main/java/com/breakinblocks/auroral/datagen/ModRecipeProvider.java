package com.breakinblocks.auroral.datagen;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.registry.ModItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

/**
 * Data generator for crafting recipes.
 * NeoForge 21.11 uses the Runner pattern for RecipeProvider.
 */
public class ModRecipeProvider extends RecipeProvider {
    private final HolderGetter<Item> items;

    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.items = registries.lookupOrThrow(Registries.ITEM);
    }

    @Override
    protected void buildRecipes() {
        // Glacial Basin: Stone + Blue Ice + Iron base
        ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS, ModBlocks.GLACIAL_BASIN.get())
            .pattern("S S")
            .pattern("SIS")
            .pattern("NNN")
            .define('S', Blocks.STONE)
            .define('I', Blocks.BLUE_ICE)
            .define('N', Items.IRON_INGOT)
            .unlockedBy("has_blue_ice", has(Blocks.BLUE_ICE))
            .save(this.output);

        // Woven Leather: Leather + any Wool (shapeless)
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, ModItems.WOVEN_LEATHER.get())
            .requires(Items.LEATHER)
            .requires(ItemTags.WOOL)
            .unlockedBy("has_leather", has(Items.LEATHER))
            .save(this.output);

        // Unrefined Shimmersteel: Copper + Iron + Coal (shapeless)
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, ModItems.UNREFINED_SHIMMERSTEEL.get())
            .requires(Items.COPPER_INGOT)
            .requires(Items.IRON_INGOT)
            .requires(Items.COAL)
            .unlockedBy("has_copper", has(Items.COPPER_INGOT))
            .save(this.output);

        // Candied Glow Leek: Glow Leek + Sugar + Honey Bottle
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, ModItems.CANDIED_GLOW_LEEK.get())
            .requires(ModItems.GLOW_LEEK.get())
            .requires(Items.SUGAR)
            .requires(Items.HONEY_BOTTLE)
            .unlockedBy("has_glow_leek", has(ModItems.GLOW_LEEK.get()))
            .save(this.output);

        // Frosted Cookies: Cookie + Frozen Petals
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, ModItems.FROSTED_COOKIES.get(), 4)
            .requires(Items.COOKIE)
            .requires(Items.COOKIE)
            .requires(Items.COOKIE)
            .requires(Items.COOKIE)
            .requires(ModItems.FROZEN_PETALS.get())
            .unlockedBy("has_frozen_petals", has(ModItems.FROZEN_PETALS.get()))
            .save(this.output);

        // Glow Leek Seeds: Frozen Petals + Wheat Seeds
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, ModItems.GLOW_LEEK_SEEDS.get(), 2)
            .requires(ModItems.FROZEN_PETALS.get())
            .requires(Items.WHEAT_SEEDS)
            .unlockedBy("has_frozen_petals", has(ModItems.FROZEN_PETALS.get()))
            .save(this.output);

        // Aurora Lantern: Iron Nuggets + Aurora Shard + Glass Pane
        ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS, ModBlocks.AURORA_LANTERN.get())
            .pattern("NNN")
            .pattern("GAS")
            .pattern("NNN")
            .define('N', Items.IRON_NUGGET)
            .define('G', Blocks.GLASS_PANE)
            .define('A', ModItems.AURORA_SHARD.get())
            .define('S', ModItems.SHIMMERSTEEL_INGOT.get())
            .unlockedBy("has_aurora_shard", has(ModItems.AURORA_SHARD.get()))
            .save(this.output);

        // Hot Cocoa: Milk Bucket + Cocoa Beans + Sugar (shapeless)
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.FOOD, ModItems.HOT_COCOA.get())
            .requires(Items.MILK_BUCKET)
            .requires(Items.COCOA_BEANS)
            .requires(Items.SUGAR)
            .unlockedBy("has_cocoa_beans", has(Items.COCOA_BEANS))
            .save(this.output);

        // Shimmersteel Tool Smithing Recipes (Iron Tool + Shimmersteel Ingot + Template)

        // Shimmersteel Hoe: Iron Hoe upgraded with Shimmersteel
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get()),
                Ingredient.of(Items.IRON_HOE),
                Ingredient.of(ModItems.SHIMMERSTEEL_INGOT.get()),
                RecipeCategory.TOOLS,
                ModItems.SHIMMERSTEEL_HOE.get())
            .unlocks("has_shimmersteel_ingot", has(ModItems.SHIMMERSTEEL_INGOT.get()))
            .save(this.output, Auroral.MOD_ID + ":shimmersteel_hoe_smithing");

        // Shimmersteel Pickaxe: Iron Pickaxe upgraded with Shimmersteel
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get()),
                Ingredient.of(Items.IRON_PICKAXE),
                Ingredient.of(ModItems.SHIMMERSTEEL_INGOT.get()),
                RecipeCategory.TOOLS,
                ModItems.SHIMMERSTEEL_PICKAXE.get())
            .unlocks("has_shimmersteel_ingot", has(ModItems.SHIMMERSTEEL_INGOT.get()))
            .save(this.output, Auroral.MOD_ID + ":shimmersteel_pickaxe_smithing");

        // Shimmersteel Axe: Iron Axe upgraded with Shimmersteel
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get()),
                Ingredient.of(Items.IRON_AXE),
                Ingredient.of(ModItems.SHIMMERSTEEL_INGOT.get()),
                RecipeCategory.TOOLS,
                ModItems.SHIMMERSTEEL_AXE.get())
            .unlocks("has_shimmersteel_ingot", has(ModItems.SHIMMERSTEEL_INGOT.get()))
            .save(this.output, Auroral.MOD_ID + ":shimmersteel_axe_smithing");

        // Shimmersteel Shovel: Iron Shovel upgraded with Shimmersteel
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get()),
                Ingredient.of(Items.IRON_SHOVEL),
                Ingredient.of(ModItems.SHIMMERSTEEL_INGOT.get()),
                RecipeCategory.TOOLS,
                ModItems.SHIMMERSTEEL_SHOVEL.get())
            .unlocks("has_shimmersteel_ingot", has(ModItems.SHIMMERSTEEL_INGOT.get()))
            .save(this.output, Auroral.MOD_ID + ":shimmersteel_shovel_smithing");

        // Shimmersteel Sword: Iron Sword upgraded with Shimmersteel
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get()),
                Ingredient.of(Items.IRON_SWORD),
                Ingredient.of(ModItems.SHIMMERSTEEL_INGOT.get()),
                RecipeCategory.COMBAT,
                ModItems.SHIMMERSTEEL_SWORD.get())
            .unlocks("has_shimmersteel_ingot", has(ModItems.SHIMMERSTEEL_INGOT.get()))
            .save(this.output, Auroral.MOD_ID + ":shimmersteel_sword_smithing");

        // Shimmer Spear: Iron Spear upgraded with Shimmersteel (smithing)
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get()),
                Ingredient.of(Items.IRON_SPEAR),
                Ingredient.of(ModItems.SHIMMERSTEEL_INGOT.get()),
                RecipeCategory.COMBAT,
                ModItems.SHIMMER_SPEAR.get())
            .unlocks("has_shimmersteel_ingot", has(ModItems.SHIMMERSTEEL_INGOT.get()))
            .save(this.output, Auroral.MOD_ID + ":shimmer_spear_smithing");

        // Shimmersteel Bow: Bow upgraded with Shimmersteel (smithing)
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get()),
                Ingredient.of(Items.BOW),
                Ingredient.of(ModItems.SHIMMERSTEEL_INGOT.get()),
                RecipeCategory.COMBAT,
                ModItems.SHIMMERSTEEL_BOW.get())
            .unlocks("has_shimmersteel_ingot", has(ModItems.SHIMMERSTEEL_INGOT.get()))
            .save(this.output, Auroral.MOD_ID + ":shimmersteel_bow_smithing");

        // Shimmerweave Armor Smithing Recipes (Iron Armor + Shimmerweave Fabric + Template)

        // Shimmerweave Goggles: Iron Helmet upgraded with Shimmerweave
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get()),
                Ingredient.of(Items.IRON_HELMET),
                Ingredient.of(ModItems.SHIMMERWEAVE_FABRIC.get()),
                RecipeCategory.COMBAT,
                ModItems.SHIMMERWEAVE_GOGGLES.get())
            .unlocks("has_shimmerweave_fabric", has(ModItems.SHIMMERWEAVE_FABRIC.get()))
            .save(this.output, Auroral.MOD_ID + ":shimmerweave_goggles_smithing");

        // Shimmerweave Tunic: Iron Chestplate upgraded with Shimmerweave
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get()),
                Ingredient.of(Items.IRON_CHESTPLATE),
                Ingredient.of(ModItems.SHIMMERWEAVE_FABRIC.get()),
                RecipeCategory.COMBAT,
                ModItems.SHIMMERWEAVE_TUNIC.get())
            .unlocks("has_shimmerweave_fabric", has(ModItems.SHIMMERWEAVE_FABRIC.get()))
            .save(this.output, Auroral.MOD_ID + ":shimmerweave_tunic_smithing");

        // Shimmerweave Leggings: Iron Leggings upgraded with Shimmerweave
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get()),
                Ingredient.of(Items.IRON_LEGGINGS),
                Ingredient.of(ModItems.SHIMMERWEAVE_FABRIC.get()),
                RecipeCategory.COMBAT,
                ModItems.SHIMMERWEAVE_LEGGINGS.get())
            .unlocks("has_shimmerweave_fabric", has(ModItems.SHIMMERWEAVE_FABRIC.get()))
            .save(this.output, Auroral.MOD_ID + ":shimmerweave_leggings_smithing");

        // Shimmerweave Skates: Iron Boots upgraded with Shimmerweave
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(ModItems.SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE.get()),
                Ingredient.of(Items.IRON_BOOTS),
                Ingredient.of(ModItems.SHIMMERWEAVE_FABRIC.get()),
                RecipeCategory.COMBAT,
                ModItems.SHIMMERWEAVE_SKATES.get())
            .unlocks("has_shimmerweave_fabric", has(ModItems.SHIMMERWEAVE_FABRIC.get()))
            .save(this.output, Auroral.MOD_ID + ":shimmerweave_skates_smithing");

        // Snow Block from Snowballs (ring pattern)
        ShapedRecipeBuilder.shaped(items, RecipeCategory.BUILDING_BLOCKS, Blocks.SNOW_BLOCK)
            .pattern("SSS")
            .pattern("S S")
            .pattern("SSS")
            .define('S', Items.SNOWBALL)
            .unlockedBy("has_snowball", has(Items.SNOWBALL))
            .save(this.output, Auroral.MOD_ID + ":snow_block_from_snowballs");

        // Cold Brewing Stand: Frozen Petal + 3 Shimmersteel Ingots (like Brewing Stand but with frozen petal)
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModBlocks.COLD_BREWING_STAND.get())
            .pattern(" F ")
            .pattern("SSS")
            .define('F', ModItems.FROZEN_PETALS.get())
            .define('S', ModItems.SHIMMERSTEEL_INGOT.get())
            .unlockedBy("has_frozen_petals", has(ModItems.FROZEN_PETALS.get()))
            .save(this.output);

        // Hearthwood Log: Like campfire but with Aurora Shard instead of coal
        ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS, ModBlocks.HEARTHWOOD_LOG.get())
            .pattern(" S ")
            .pattern("SAS")
            .pattern("LLL")
            .define('S', Items.STICK)
            .define('A', ModItems.AURORA_SHARD.get())
            .define('L', ItemTags.LOGS_THAT_BURN)
            .unlockedBy("has_aurora_shard", has(ModItems.AURORA_SHARD.get()))
            .save(this.output);
    }

    /**
     * Runner class for registering with GatherDataEvent.
     */
    public static class Runner extends RecipeProvider.Runner {

        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Auroral Recipes";
        }
    }
}
