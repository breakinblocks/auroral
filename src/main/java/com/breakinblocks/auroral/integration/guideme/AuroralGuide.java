package com.breakinblocks.auroral.integration.guideme;

import com.breakinblocks.auroral.Auroral;
import guideme.Guide;
import guideme.Guides;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Integration with the GuideME guidebook mod.
 * Provides an in-game guidebook for Auroral.
 */
public class AuroralGuide {

    /**
     * The guide ID for the Auroral guidebook.
     */
    public static final ResourceLocation GUIDE_ID = Auroral.id("guide");

    private static Guide guide;

    /**
     * Initialize GuideME integration.
     * Creates and registers the Auroral guidebook.
     */
    public static void init() {
        Auroral.LOGGER.info("Initializing GuideME integration for Auroral");

        guide = Guide.builder(GUIDE_ID)
            .defaultNamespace(Auroral.MOD_ID)
            .folder("guide")
            .startPage(Auroral.id("index"))
            .register(true)
            .build();

        Auroral.LOGGER.info("Auroral guidebook registered");
    }

    /**
     * Check if GuideME is available.
     */
    public static boolean isGuideAvailable() {
        return guide != null;
    }

    /**
     * Gets the guide instance.
     */
    public static Guide getGuide() {
        return guide;
    }

    /**
     * Creates a guide item for the creative tab.
     * Returns empty stack if guide is not available.
     */
    public static ItemStack createGuideItem() {
        if (guide != null) {
            return Guides.createGuideItem(GUIDE_ID);
        }
        return ItemStack.EMPTY;
    }
}
