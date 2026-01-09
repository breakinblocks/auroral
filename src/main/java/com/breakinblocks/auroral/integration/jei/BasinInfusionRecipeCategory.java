package com.breakinblocks.auroral.integration.jei;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI recipe category for Basin Infusion recipes.
 */
public class BasinInfusionRecipeCategory implements IRecipeCategory<BasinInfusionRecipe> {
    public static final ResourceLocation UID = Auroral.id("basin_infusion");
    public static final RecipeType<BasinInfusionRecipe> RECIPE_TYPE =
        RecipeType.create(Auroral.MOD_ID, "basin_infusion", BasinInfusionRecipe.class);

    private final IDrawable icon;
    private final Component title;
    private final int width = 120;
    private final int height = 40;

    public BasinInfusionRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
            new ItemStack(ModBlocks.GLACIAL_BASIN.get()));
        this.title = Component.translatable("auroral.jei.category.basin_infusion");
    }

    @Override
    public RecipeType<BasinInfusionRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BasinInfusionRecipe recipe, IFocusGroup focuses) {
        // Input slot (left side)
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 12)
            .addItemStack(recipe.input());

        // Basin icon (center) - render only as decoration
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 52, 12)
            .addItemStack(new ItemStack(ModBlocks.GLACIAL_BASIN.get()))
            .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                tooltip.add(Component.translatable("auroral.jei.basin_infusion.tooltip"));
            });

        // Output slot (right side)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 12)
            .addItemStack(recipe.output());
    }
}
