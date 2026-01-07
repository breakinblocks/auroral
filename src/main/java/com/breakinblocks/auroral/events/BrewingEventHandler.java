package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModItems;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

/**
 * Handles registration of custom brewing recipes.
 * Frozen Petals can be used like Nether Wart to create Awkward Potions.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID)
public class BrewingEventHandler {

    /**
     * Register brewing recipes.
     * Frozen Petals convert Water Bottles to Awkward Potions, just like Nether Wart.
     */
    @SubscribeEvent
    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();

        // Frozen Petals: Water Bottle → Awkward Potion (like Nether Wart)
        builder.addMix(Potions.WATER, ModItems.FROZEN_PETALS.get(), Potions.AWKWARD);

        Auroral.LOGGER.debug("Registered Auroral brewing recipes");
    }
}
