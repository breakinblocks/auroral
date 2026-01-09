package com.breakinblocks.auroral.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * Shimmerweave Leggings - Leg armor with special abilities.
 *
 * Special Abilities:
 * <ul>
 *   <li>Speed I when walking on snow blocks</li>
 *   <li>Soul Speed when walking on soul sand/soul soil (like the enchantment)</li>
 * </ul>
 *
 * The speed effects are handled via event handler in ShimmerweaveEventHandler.
 */
public class ShimmerweaveLeggingsItem extends ArmorItem {

    /**
     * Duration of speed effect in ticks (refreshed while on snow).
     */
    public static final int SPEED_DURATION = 40; // 2 seconds, refreshed continuously

    /**
     * Duration of soul speed effect in ticks.
     */
    public static final int SOUL_SPEED_DURATION = 40; // 2 seconds, refreshed continuously

    public ShimmerweaveLeggingsItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
    }
}
