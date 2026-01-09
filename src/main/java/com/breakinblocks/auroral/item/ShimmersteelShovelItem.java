package com.breakinblocks.auroral.item;

import net.minecraft.world.item.ShovelItem;

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
public class ShimmersteelShovelItem extends ShovelItem {

    public ShimmersteelShovelItem(Properties properties) {
        super(ModToolTiers.SHIMMERSTEEL, properties);
    }

    /**
     * Checks if this item should apply silk touch effect.
     * Always returns true for Shimmersteel Shovel.
     */
    public boolean hasInherentSilkTouch() {
        return true;
    }
}
