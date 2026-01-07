package com.breakinblocks.auroral.integration.guideme;

import com.breakinblocks.auroral.Auroral;
import guideme.Guide;
import guideme.GuideItemSettings;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * Integration with the GuideME guidebook mod.
 *
 * The guide content is located in: assets/auroral/guide/
 *
 * GuideME uses markdown files for content authoring with support for:
 * - Simple markdown formatting
 * - 3D scene embedding from structure files
 * - Recipe displays
 * - Cross-linking between topics
 *
 * @see <a href="https://guideme.appliedenergistics.org">GuideME Documentation</a>
 */
public class AuroralGuide {

    public static final ResourceLocation GUIDE_ID = Auroral.id("guide");

    private static Guide guide;

    /**
     * Initialize GuideME integration.
     * This method should be called during mod initialization when GuideME is available.
     */
    public static void init() {
        try {
            guide = Guide.builder(GUIDE_ID)
                .defaultNamespace(Auroral.MOD_ID)
                .folder("guide")  // Content is at assets/auroral/guide/
                .itemSettings(new GuideItemSettings(
                    Optional.empty(),  // Use default display name
                    List.of(),         // No extra tooltip lines
                    Optional.of(Auroral.id("item/guide"))  // Custom item model
                ))
                .build();
            Auroral.LOGGER.info("Auroral GuideME guide registered");
        } catch (Exception e) {
            Auroral.LOGGER.warn("Failed to initialize GuideME integration: {}", e.getMessage());
        }
    }

    /**
     * Get the guide instance.
     */
    public static Guide getGuide() {
        return guide;
    }

    /**
     * Check if GuideME is available.
     */
    public static boolean isGuideAvailable() {
        try {
            Class.forName("guideme.Guide");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
