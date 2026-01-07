package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.registry.ModTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * Shimmersteel Pickaxe with inherent Fortune III for gems only.
 *
 * When mining gem-type ores (diamond, emerald, lapis, redstone, amethyst, quartz),
 * this pickaxe applies Fortune III bonus drops even without the enchantment.
 *
 * The Fortune effect is applied via event handler in ShimmersteelEventHandler.
 */
public class ShimmersteelPickaxeItem extends Item {

    /**
     * Set of blocks that count as "gems" for the Fortune bonus.
     */
    private static final Set<Block> GEM_BLOCKS = Set.of(
        Blocks.DIAMOND_ORE,
        Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.EMERALD_ORE,
        Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.LAPIS_ORE,
        Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.REDSTONE_ORE,
        Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.NETHER_QUARTZ_ORE,
        Blocks.AMETHYST_CLUSTER,
        Blocks.LARGE_AMETHYST_BUD,
        Blocks.MEDIUM_AMETHYST_BUD,
        Blocks.SMALL_AMETHYST_BUD
    );

    public ShimmersteelPickaxeItem(Properties properties) {
        super(properties.pickaxe(ModToolTiers.SHIMMERSTEEL, 1.0f, -2.8f));
    }

    /**
     * Checks if a block is a gem-type block that should receive Fortune bonus.
     */
    public static boolean isGemBlock(BlockState state) {
        // Check hardcoded set first
        if (GEM_BLOCKS.contains(state.getBlock())) {
            return true;
        }
        // Also check the tag for modded gems
        return state.is(ModTags.Blocks.GEM_ORES);
    }

    /**
     * Gets the effective Fortune level for this pickaxe when mining the given block.
     * Returns 3 (Fortune III) for gem blocks, 0 otherwise (unless enchanted).
     */
    public static int getEffectiveFortuneLevel(BlockState state) {
        if (isGemBlock(state)) {
            return 3;
        }
        return 0;
    }
}
