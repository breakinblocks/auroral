package com.breakinblocks.auroral.integration.jei;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI plugin for Auroral.
 * Registers basin infusion recipes and categories.
 */
@JeiPlugin
public class AuroralJeiPlugin implements IModPlugin {
    private static final Identifier PLUGIN_ID = Auroral.id("jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
            new BasinInfusionRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<BasinInfusionRecipe> basinRecipes = new ArrayList<>();

        // Unrefined Shimmersteel -> Shimmersteel Ingot
        basinRecipes.add(new BasinInfusionRecipe(
            new ItemStack(ModItems.UNREFINED_SHIMMERSTEEL.get()),
            new ItemStack(ModItems.SHIMMERSTEEL_INGOT.get())
        ));

        // Woven Leather -> Shimmerweave Fabric
        basinRecipes.add(new BasinInfusionRecipe(
            new ItemStack(ModItems.WOVEN_LEATHER.get()),
            new ItemStack(ModItems.SHIMMERWEAVE_FABRIC.get())
        ));

        // Frozen Petals -> Aurora Shard
        basinRecipes.add(new BasinInfusionRecipe(
            new ItemStack(ModItems.FROZEN_PETALS.get()),
            new ItemStack(ModItems.AURORA_SHARD.get())
        ));

        // Ice -> Shimmering Ice
        basinRecipes.add(new BasinInfusionRecipe(
            new ItemStack(Items.ICE),
            new ItemStack(ModBlocks.SHIMMERING_ICE.asItem())
        ));

        registration.addRecipes(BasinInfusionRecipeCategory.RECIPE_TYPE, basinRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Register the Glacial Basin as a crafting station for basin infusion recipes
        registration.addCraftingStation(
            BasinInfusionRecipeCategory.RECIPE_TYPE,
            ModBlocks.GLACIAL_BASIN.get()
        );

        // Register the Cold Brewing Stand as a catalyst for brewing recipes
        // This allows JEI to show brewing recipes when pressing U on the Cold Brewing Stand
        registration.addCraftingStation(
            RecipeTypes.BREWING,
            ModBlocks.COLD_BREWING_STAND.get()
        );
    }
}
