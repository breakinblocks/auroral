package com.breakinblocks.auroral.registry;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.block.ColdBrewingStandBlockEntity;
import com.breakinblocks.auroral.block.GlacialBasinBlockEntity;
import com.breakinblocks.auroral.block.HearthwoodLogBlockEntity;
import com.breakinblocks.auroral.block.SnowAngelBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Auroral.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GlacialBasinBlockEntity>> GLACIAL_BASIN =
        BLOCK_ENTITIES.register("glacial_basin", () ->
            new BlockEntityType<>(GlacialBasinBlockEntity::new, Set.of(ModBlocks.GLACIAL_BASIN.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ColdBrewingStandBlockEntity>> COLD_BREWING_STAND =
        BLOCK_ENTITIES.register("cold_brewing_stand", () ->
            new BlockEntityType<>(ColdBrewingStandBlockEntity::new, Set.of(ModBlocks.COLD_BREWING_STAND.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HearthwoodLogBlockEntity>> HEARTHWOOD_LOG =
        BLOCK_ENTITIES.register("hearthwood_log", () ->
            new BlockEntityType<>(HearthwoodLogBlockEntity::new, Set.of(ModBlocks.HEARTHWOOD_LOG.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SnowAngelBlockEntity>> SNOW_ANGEL =
        BLOCK_ENTITIES.register("snow_angel", () ->
            new BlockEntityType<>(SnowAngelBlockEntity::new, Set.of(ModBlocks.SNOW_ANGEL.get())));
}
