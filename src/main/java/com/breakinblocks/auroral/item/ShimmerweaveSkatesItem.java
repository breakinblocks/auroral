package com.breakinblocks.auroral.item;

import net.minecraft.world.item.Item;

/**
 * Shimmerweave Skates - Boots armor with multiple special abilities.
 *
 * Special Abilities:
 * <ul>
 *   <li>Speed I when on ice or snow blocks</li>
 *   <li>Reduced friction on ice (skate smoothly)</li>
 *   <li>Frost Walker effect (freeze water under feet)</li>
 *   <li>Extended Frost Walker: Lava turns to Obsidian</li>
 *   <li>Fall damage immunity when landing on ice or obsidian</li>
 * </ul>
 *
 * The special effects are handled via event handler in ShimmerweaveEventHandler.
 */
public class ShimmerweaveSkatesItem extends Item {

    /**
     * Duration of speed effect in ticks (refreshed while on ice/snow).
     */
    public static final int SPEED_DURATION = 40; // 2 seconds

    /**
     * Frost Walker radius (blocks around player to freeze).
     */
    public static final int FROST_WALKER_RADIUS = 2;

    public ShimmerweaveSkatesItem(Properties properties) {
        super(properties);
    }
}
