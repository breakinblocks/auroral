package com.breakinblocks.auroral.item;

import net.minecraft.world.item.Item;

/**
 * Shimmersteel Shovel with inherent Silk Touch effect.
 *
 * When mining blocks that would normally be affected by Silk Touch
 * (snow, grass blocks, mycelium, etc.), this shovel always drops
 * the block itself instead of the normal drop.
 *
 * The silk touch behavior is implemented via event handler in
 * {@link com.breakinblocks.auroral.events.ShimmersteelEventHandler}.
 */
public class ShimmersteelShovelItem extends Item {

    public ShimmersteelShovelItem(Properties properties) {
        super(properties.shovel(ModToolTiers.SHIMMERSTEEL, 1.5f, -3.0f));
    }

    /**
     * Checks if this item should apply silk touch effect.
     * Always returns true for Shimmersteel Shovel.
     */
    public boolean hasInherentSilkTouch() {
        return true;
    }
}
