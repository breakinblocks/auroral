package com.breakinblocks.auroral.integration.guideme;

import com.breakinblocks.auroral.Auroral;
import guideme.Guide;
import guideme.GuidesCommon;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class AuroralGuide {

    public static final Identifier GUIDE_ID = Auroral.id("guide");

    private static Guide guide;

    public static void init() {
        try {
            guide = Guide.builder(GUIDE_ID)
                .defaultNamespace(Auroral.MOD_ID)
                .folder("auroral")
                .build();
            Auroral.LOGGER.info("Auroral GuideME guide registered");
        } catch (Exception e) {
            Auroral.LOGGER.warn("Failed to initialize GuideME integration: {}", e.getMessage());
        }
    }

    public static Guide getGuide() {
        return guide;
    }

    public static void openGuide(Player player) {
        GuidesCommon.openGuide(player, GUIDE_ID);
    }

    public static boolean isGuideAvailable() {
        try {
            Class.forName("guideme.Guide");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
