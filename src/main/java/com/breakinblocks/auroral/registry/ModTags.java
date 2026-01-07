package com.breakinblocks.auroral.registry;

import com.breakinblocks.auroral.Auroral;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Custom tags for Auroral.
 */
public class ModTags {

    public static class Items {
        /**
         * Tag for items that can repair Shimmersteel tools.
         */
        public static final TagKey<Item> SHIMMERSTEEL_REPAIR = tag("shimmersteel_repair");

        /**
         * Tag for items that can repair Shimmerweave armor.
         */
        public static final TagKey<Item> SHIMMERWEAVE_REPAIR = tag("shimmerweave_repair");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, Auroral.id(name));
        }
    }

    public static class Blocks {
        /**
         * Tag for gem ore blocks that get Fortune bonus from Shimmersteel Pickaxe.
         */
        public static final TagKey<Block> GEM_ORES = tag("gem_ores");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, Auroral.id(name));
        }
    }
}
