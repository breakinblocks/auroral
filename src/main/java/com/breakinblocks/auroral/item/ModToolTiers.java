package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.registry.ModTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

/**
 * Tool tiers for Auroral tools.
 */
public class ModToolTiers {

    /**
     * Shimmersteel tool tier.
     * Slightly better than Iron, with high enchantability.
     *
     * Stats compared to Iron:
     * - Iron: durability 250, speed 6.0, damage 2.0, enchantability 14
     * - Shimmersteel: durability 350, speed 6.5, damage 2.5, enchantability 22
     */
    public static final ToolMaterial SHIMMERSTEEL = new ToolMaterial(
        BlockTags.INCORRECT_FOR_IRON_TOOL, // Same mining level as iron
        350,    // Durability (iron is 250)
        6.5f,   // Mining speed (iron is 6.0)
        2.5f,   // Attack damage bonus (iron is 2.0)
        22,     // Enchantability (iron is 14, gold is 22)
        ModTags.Items.SHIMMERSTEEL_REPAIR // Repair ingredient tag
    );
}
