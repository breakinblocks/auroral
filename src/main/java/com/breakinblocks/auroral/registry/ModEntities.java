package com.breakinblocks.auroral.registry;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.entity.AuroralNautilusEntity;
import com.breakinblocks.auroral.entity.AuroralSnowletteEntity;
import com.breakinblocks.auroral.entity.StarShotEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(Registries.ENTITY_TYPE, Auroral.MOD_ID);

    // Star-Shot Projectile
    public static final ResourceKey<EntityType<?>> STAR_SHOT_KEY =
        ResourceKey.create(Registries.ENTITY_TYPE, Auroral.id("star_shot"));

    public static final Supplier<EntityType<StarShotEntity>> STAR_SHOT =
        ENTITIES.register("star_shot", () ->
            EntityType.Builder.<StarShotEntity>of(StarShotEntity::new, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build("auroral:star_shot"));

    // Auroral Nautilus - Flying ambient creature during Aurora
    public static final ResourceKey<EntityType<?>> AURORAL_NAUTILUS_KEY =
        ResourceKey.create(Registries.ENTITY_TYPE, Auroral.id("auroral_nautilus"));

    public static final Supplier<EntityType<AuroralNautilusEntity>> AURORAL_NAUTILUS =
        ENTITIES.register("auroral_nautilus", () ->
            EntityType.Builder.<AuroralNautilusEntity>of(AuroralNautilusEntity::new, MobCategory.AMBIENT)
                .sized(0.8F, 0.6F)
                .eyeHeight(0.3F)
                .clientTrackingRange(8)
                .updateInterval(3)
                .build("auroral:auroral_nautilus"));

    // Auroral Snowlette - Small pet Snow Golem variant
    public static final ResourceKey<EntityType<?>> AURORAL_SNOWLETTE_KEY =
        ResourceKey.create(Registries.ENTITY_TYPE, Auroral.id("auroral_snowlette"));

    public static final Supplier<EntityType<AuroralSnowletteEntity>> AURORAL_SNOWLETTE =
        ENTITIES.register("auroral_snowlette", () ->
            EntityType.Builder.<AuroralSnowletteEntity>of(AuroralSnowletteEntity::new, MobCategory.CREATURE)
                .sized(0.175F, 0.5F)  // 1/4 size of Snow Golem (0.7, 1.9)
                .eyeHeight(0.4F)
                .clientTrackingRange(8)
                .updateInterval(3)
                .build("auroral:auroral_snowlette"));
}
