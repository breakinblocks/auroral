package com.breakinblocks.auroral.registry;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.block.*;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Auroral.MOD_ID);

    // Glacial Basin - primary workstation
    // Using registerBlock instead of register to properly set block ID before construction
    public static final DeferredBlock<GlacialBasinBlock> GLACIAL_BASIN = BLOCKS.registerBlock("glacial_basin",
        props -> new GlacialBasinBlock(props
            .strength(2.0f, 6.0f)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .lightLevel(state -> state.getValue(GlacialBasinBlock.AURA_LEVEL) * 3)
        ));

    // Cold Brewing Stand - Nether-free brewing alternative
    public static final DeferredBlock<ColdBrewingStandBlock> COLD_BREWING_STAND = BLOCKS.registerBlock("cold_brewing_stand",
        props -> new ColdBrewingStandBlock(props
            .strength(0.5f)
            .sound(SoundType.METAL)
            .noOcclusion()
            .lightLevel(state -> 1)
        ));

    // Hearthwood Log - Burns for 7 days, provides warmth and frostbite immunity
    public static final DeferredBlock<HearthwoodLogBlock> HEARTHWOOD_LOG = BLOCKS.registerBlock("hearthwood_log",
        props -> new HearthwoodLogBlock(props
            .mapColor(MapColor.WOOD)
            .strength(2.0f)
            .sound(SoundType.WOOD)
            .noOcclusion()
            .lightLevel(state -> state.getValue(HearthwoodLogBlock.LIT) ? 15 : 0)
        ));

    // Shimmering Ice - Glowing ice that never melts, hydrates farmland
    public static final DeferredBlock<ShimmeringIceBlock> SHIMMERING_ICE = BLOCKS.registerBlock("shimmering_ice",
        props -> new ShimmeringIceBlock(props
            .mapColor(MapColor.ICE)
            .strength(0.5f)
            .friction(0.98f)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .lightLevel(state -> 8)
        ));

    // Glow-Leek - Bioluminescent crop grown on Shimmering Ice (no block item - uses seeds)
    public static final DeferredBlock<GlowLeekBlock> GLOW_LEEK = BLOCKS.registerBlock("glow_leek",
        props -> new GlowLeekBlock(props
            .mapColor(MapColor.PLANT)
            .noCollision()
            .instabreak()
            .sound(SoundType.CROP)
            .pushReaction(PushReaction.DESTROY)
        ));

    // Aurora Bloom - Spawns on snow during Aurora, drops Frozen Petals when fully grown
    public static final DeferredBlock<AuroraBloomBlock> AURORA_BLOOM = BLOCKS.registerBlock("aurora_bloom",
        props -> new AuroraBloomBlock(props
            .mapColor(MapColor.SNOW)
            .noCollision()
            .instabreak()
            .sound(SoundType.GRASS)
            .pushReaction(PushReaction.DESTROY)
            .randomTicks()
        ));

    // Aurora Lantern - Decorative light source that glows with aurora colors
    public static final DeferredBlock<AuroraLanternBlock> AURORA_LANTERN = BLOCKS.registerBlock("aurora_lantern",
        props -> new AuroraLanternBlock(props
            .mapColor(MapColor.COLOR_LIGHT_BLUE)
            .strength(0.3f)
            .sound(SoundType.LANTERN)
            .noOcclusion()
            .lightLevel(state -> 15)
        ));

    // Snow Angel - Decorative imprint created by sneak + right-clicking on snow
    public static final DeferredBlock<SnowAngelBlock> SNOW_ANGEL = BLOCKS.registerBlock("snow_angel",
        props -> new SnowAngelBlock(props
            .mapColor(MapColor.SNOW)
            .noCollision()
            .instabreak()
            .sound(SoundType.SNOW)
            .pushReaction(PushReaction.DESTROY)
        ));

    // Shimmer Soil - Aurora-infused farmland that accelerates crop growth
    public static final DeferredBlock<ShimmerSoilBlock> SHIMMER_SOIL = BLOCKS.registerBlock("shimmer_soil",
        props -> new ShimmerSoilBlock(props
            .mapColor(MapColor.COLOR_LIGHT_BLUE)
            .strength(0.6f)
            .sound(SoundType.GRAVEL)
            .randomTicks()
        ));
}
