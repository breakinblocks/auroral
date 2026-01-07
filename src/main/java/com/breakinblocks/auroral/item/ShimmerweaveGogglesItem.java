package com.breakinblocks.auroral.item;

import net.minecraft.world.item.Item;

/**
 * Shimmerweave Goggles - Head armor with special ability.
 *
 * Special Ability: All hostile mobs within 32 blocks receive the Glowing effect,
 * allowing the wearer to see them through walls.
 *
 * The glowing effect is applied via event handler in ShimmerweaveEventHandler.
 *
 * Future enhancement: Also equippable in Curios slot when that mod is present.
 */
public class ShimmerweaveGogglesItem extends Item {

    /**
     * Radius within which hostile mobs receive glowing effect.
     */
    public static final double GLOWING_RADIUS = 32.0;

    /**
     * Duration of glowing effect in ticks (refreshed every tick while wearing).
     */
    public static final int GLOWING_DURATION = 40; // 2 seconds, refreshed continuously

    public ShimmerweaveGogglesItem(Properties properties) {
        super(properties);
    }
}
