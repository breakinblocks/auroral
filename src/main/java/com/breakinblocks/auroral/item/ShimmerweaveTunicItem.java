package com.breakinblocks.auroral.item;

import net.minecraft.world.item.Item;

/**
 * Shimmerweave Tunic - Chestplate armor with special ability.
 *
 * Special Ability: Automatically extinguishes fire on the wearer.
 * When the player catches fire while wearing this tunic, the fire
 * is immediately put out.
 *
 * The fire extinguishing is handled via event handler in ShimmerweaveEventHandler.
 */
public class ShimmerweaveTunicItem extends Item {

    /**
     * Cooldown between fire extinguish effects in ticks (to prevent spam).
     */
    public static final int EXTINGUISH_COOLDOWN = 20; // 1 second

    public ShimmerweaveTunicItem(Properties properties) {
        super(properties);
    }
}
