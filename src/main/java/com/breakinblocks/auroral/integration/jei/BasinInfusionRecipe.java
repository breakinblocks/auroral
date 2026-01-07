package com.breakinblocks.auroral.integration.jei;

import net.minecraft.world.item.ItemStack;

/**
 * Simple data class representing a basin infusion recipe for JEI display.
 */
public record BasinInfusionRecipe(ItemStack input, ItemStack output) {
}
