package com.breakinblocks.auroral.client.gui;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.inventory.ColdBrewingStandMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for the Cold Brewing Stand.
 * Uses the same layout as vanilla brewing stand but with cold/winter theming.
 */
public class ColdBrewingStandScreen extends AbstractContainerScreen<ColdBrewingStandMenu> {
    private static final ResourceLocation FUEL_LENGTH_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/fuel_length");
    private static final ResourceLocation BREW_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/brew_progress");
    private static final ResourceLocation BUBBLES_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/bubbles");

    /**
     * Use vanilla brewing stand texture as base.
     * The snowball icon is rendered via the slot's getNoItemIcon() method.
     */
    private static final ResourceLocation BREWING_STAND_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/brewing_stand.png");

    /**
     * Bubble animation lengths (same as vanilla).
     */
    private static final int[] BUBBLELENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

    public ColdBrewingStandScreen(ColdBrewingStandMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw the background texture
        graphics.blit(BREWING_STAND_LOCATION, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Draw the fuel bar
        int fuel = this.menu.getFuel();
        int fuelLength = Mth.clamp((18 * fuel + 20 - 1) / 20, 0, 18);
        if (fuelLength > 0) {
            graphics.blitSprite(FUEL_LENGTH_SPRITE, 18, 4, 0, 0, x + 60, y + 44, fuelLength, 4);
        }

        // Draw brew progress and bubbles
        int brewingTicks = this.menu.getBrewingTicks();
        if (brewingTicks > 0) {
            // Progress arrow
            int progress = (int)(28.0F * (1.0F - brewingTicks / 400.0F));
            if (progress > 0) {
                graphics.blitSprite(BREW_PROGRESS_SPRITE, 9, 28, 0, 0, x + 97, y + 16, 9, progress);
            }

            // Bubbles animation
            int bubbleHeight = BUBBLELENGTHS[brewingTicks / 2 % 7];
            if (bubbleHeight > 0) {
                graphics.blitSprite(BUBBLES_SPRITE, 12, 29, 0, 29 - bubbleHeight, x + 63, y + 14 + 29 - bubbleHeight, 12, bubbleHeight);
            }
        }
    }
}
