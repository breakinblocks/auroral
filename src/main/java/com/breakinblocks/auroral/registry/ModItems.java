package com.breakinblocks.auroral.registry;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.item.*;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Auroral.MOD_ID);

    // Block Items
    public static final DeferredItem<BlockItem> GLACIAL_BASIN_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.GLACIAL_BASIN);
    public static final DeferredItem<BlockItem> COLD_BREWING_STAND_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.COLD_BREWING_STAND);
    public static final DeferredItem<BlockItem> HEARTHWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.HEARTHWOOD_LOG);
    public static final DeferredItem<BlockItem> SHIMMERING_ICE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.SHIMMERING_ICE);
    public static final DeferredItem<BlockItem> AURORA_BLOOM_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.AURORA_BLOOM);
    public static final DeferredItem<BlockItem> AURORA_LANTERN_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.AURORA_LANTERN);
    public static final DeferredItem<BlockItem> SNOW_ANGEL_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.SNOW_ANGEL);
    public static final DeferredItem<BlockItem> SHIMMER_SOIL_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.SHIMMER_SOIL);

    // Basic Materials - using registerSimpleItem for items with no custom properties
    public static final DeferredItem<Item> UNREFINED_SHIMMERSTEEL = ITEMS.registerSimpleItem("unrefined_shimmersteel");
    public static final DeferredItem<Item> SHIMMERSTEEL_INGOT = ITEMS.registerSimpleItem("shimmersteel_ingot");
    public static final DeferredItem<Item> SHIMMERWEAVE_FABRIC = ITEMS.registerSimpleItem("shimmerweave_fabric");
    public static final DeferredItem<Item> WOVEN_LEATHER = ITEMS.registerSimpleItem("woven_leather");
    public static final DeferredItem<Item> AURORA_SHARD = ITEMS.registerSimpleItem("aurora_shard");
    public static final DeferredItem<Item> FROZEN_PETALS = ITEMS.registerSimpleItem("frozen_petals");

    // Shimmersteel Upgrade Smithing Template
    public static final DeferredItem<SmithingTemplateItem> SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE = ITEMS.registerItem(
        "shimmersteel_upgrade_smithing_template",
        props -> createShimmersteelUpgradeTemplate(props));

    // Glow-Leek - food item with Night Vision + Glowing effects
    public static final DeferredItem<GlowLeekItem> GLOW_LEEK = ITEMS.registerItem("glow_leek",
        props -> new GlowLeekItem(props));

    // Glow-Leek Seeds - plants on Shimmering Ice
    public static final DeferredItem<BlockItem> GLOW_LEEK_SEEDS = ITEMS.registerSimpleBlockItem("glow_leek_seeds", ModBlocks.GLOW_LEEK);

    // Candied Glow-Leek - sweetened version with extended Night Vision, no Glowing side effect
    public static final DeferredItem<CandiedGlowLeekItem> CANDIED_GLOW_LEEK = ITEMS.registerItem("candied_glow_leek",
        props -> new CandiedGlowLeekItem(props));

    // Hot Cocoa - warm drink with Frostbite Immunity and Regeneration
    public static final DeferredItem<HotCocoaItem> HOT_COCOA = ITEMS.registerItem("hot_cocoa",
        props -> new HotCocoaItem(props));

    // Frosted Cookies - cozy snack with shimmer particles
    public static final DeferredItem<FrostedCookiesItem> FROSTED_COOKIES = ITEMS.registerItem("frosted_cookies",
        props -> new FrostedCookiesItem(props));

    // Shimmersteel Tools - using registerItem for items with custom properties
    public static final DeferredItem<ShimmersteelPickaxeItem> SHIMMERSTEEL_PICKAXE = ITEMS.registerItem("shimmersteel_pickaxe",
        props -> new ShimmersteelPickaxeItem(props));

    public static final DeferredItem<ShimmersteelAxeItem> SHIMMERSTEEL_AXE = ITEMS.registerItem("shimmersteel_axe",
        props -> new ShimmersteelAxeItem(props));

    public static final DeferredItem<ShimmersteelShovelItem> SHIMMERSTEEL_SHOVEL = ITEMS.registerItem("shimmersteel_shovel",
        props -> new ShimmersteelShovelItem(props));

    public static final DeferredItem<ShimmersteelSwordItem> SHIMMERSTEEL_SWORD = ITEMS.registerItem("shimmersteel_sword",
        props -> new ShimmersteelSwordItem(props));

    public static final DeferredItem<ShimmersteelBowItem> SHIMMERSTEEL_BOW = ITEMS.registerItem("shimmersteel_bow",
        props -> new ShimmersteelBowItem(props.durability(384))); // Similar durability to iron tools

    public static final DeferredItem<ShimmersteelHoeItem> SHIMMERSTEEL_HOE = ITEMS.registerItem("shimmersteel_hoe",
        props -> new ShimmersteelHoeItem(props));

    // Shimmerweave Armor
    public static final DeferredItem<ShimmerweaveGogglesItem> SHIMMERWEAVE_GOGGLES = ITEMS.registerItem("shimmerweave_goggles",
        props -> new ShimmerweaveGogglesItem(ModArmorMaterials.SHIMMERWEAVE, ArmorItem.Type.HELMET, props));

    public static final DeferredItem<ShimmerweaveTunicItem> SHIMMERWEAVE_TUNIC = ITEMS.registerItem("shimmerweave_tunic",
        props -> new ShimmerweaveTunicItem(ModArmorMaterials.SHIMMERWEAVE, ArmorItem.Type.CHESTPLATE, props));

    public static final DeferredItem<ShimmerweaveLeggingsItem> SHIMMERWEAVE_LEGGINGS = ITEMS.registerItem("shimmerweave_leggings",
        props -> new ShimmerweaveLeggingsItem(ModArmorMaterials.SHIMMERWEAVE, ArmorItem.Type.LEGGINGS, props));

    public static final DeferredItem<ShimmerweaveSkatesItem> SHIMMERWEAVE_SKATES = ITEMS.registerItem("shimmerweave_skates",
        props -> new ShimmerweaveSkatesItem(ModArmorMaterials.SHIMMERWEAVE, ArmorItem.Type.BOOTS, props));

    // Spawn Eggs
    public static final DeferredItem<SpawnEggItem> AURORAL_NAUTILUS_SPAWN_EGG = ITEMS.registerItem(
        "auroral_nautilus_spawn_egg",
        properties -> new SpawnEggItem(ModEntities.AURORAL_NAUTILUS.get(), 0x87CEEB, 0x40E0D0, properties));

    public static final DeferredItem<SpawnEggItem> AURORAL_SNOWLETTE_SPAWN_EGG = ITEMS.registerItem(
        "auroral_snowlette_spawn_egg",
        properties -> new SpawnEggItem(ModEntities.AURORAL_SNOWLETTE.get(), 0xF0F8FF, 0x4169E1, properties));

    // Smithing Template Helper

    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;

    // Empty slot icons for the smithing table UI
    private static final ResourceLocation EMPTY_SLOT_HELMET = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet");
    private static final ResourceLocation EMPTY_SLOT_CHESTPLATE = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate");
    private static final ResourceLocation EMPTY_SLOT_LEGGINGS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_leggings");
    private static final ResourceLocation EMPTY_SLOT_BOOTS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_boots");
    private static final ResourceLocation EMPTY_SLOT_SWORD = ResourceLocation.withDefaultNamespace("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_SLOT_PICKAXE = ResourceLocation.withDefaultNamespace("item/empty_slot_pickaxe");
    private static final ResourceLocation EMPTY_SLOT_AXE = ResourceLocation.withDefaultNamespace("item/empty_slot_axe");
    private static final ResourceLocation EMPTY_SLOT_SHOVEL = ResourceLocation.withDefaultNamespace("item/empty_slot_shovel");
    private static final ResourceLocation EMPTY_SLOT_INGOT = ResourceLocation.withDefaultNamespace("item/empty_slot_ingot");

    /**
     * Creates the Shimmersteel Upgrade Smithing Template.
     * Used to upgrade iron equipment to Shimmersteel/Shimmerweave.
     */
    private static SmithingTemplateItem createShimmersteelUpgradeTemplate(Item.Properties properties) {
        // "Applies to" description - what items can be upgraded
        Component appliesTo = Component.translatable(
            Util.makeDescriptionId("item", Auroral.id("smithing_template.shimmersteel_upgrade.applies_to"))
        ).withStyle(DESCRIPTION_FORMAT);

        // "Ingredients" description - what materials are used
        Component ingredients = Component.translatable(
            Util.makeDescriptionId("item", Auroral.id("smithing_template.shimmersteel_upgrade.ingredients"))
        ).withStyle(DESCRIPTION_FORMAT);

        // Upgrade description (title shown in smithing table)
        Component upgradeDescription = Component.translatable(
            Util.makeDescriptionId("upgrade", Auroral.id("shimmersteel_upgrade"))
        ).withStyle(TITLE_FORMAT);

        // Slot descriptions for smithing table UI
        Component baseSlotDescription = Component.translatable(
            Util.makeDescriptionId("item", Auroral.id("smithing_template.shimmersteel_upgrade.base_slot_description"))
        );
        Component additionsSlotDescription = Component.translatable(
            Util.makeDescriptionId("item", Auroral.id("smithing_template.shimmersteel_upgrade.additions_slot_description"))
        );

        // Icons for base slot (iron equipment)
        List<ResourceLocation> baseSlotIcons = List.of(
            EMPTY_SLOT_HELMET,
            EMPTY_SLOT_SWORD,
            EMPTY_SLOT_CHESTPLATE,
            EMPTY_SLOT_PICKAXE,
            EMPTY_SLOT_LEGGINGS,
            EMPTY_SLOT_AXE,
            EMPTY_SLOT_BOOTS,
            EMPTY_SLOT_SHOVEL
        );

        // Icons for addition slot (shimmersteel ingot / shimmerweave fabric)
        List<ResourceLocation> additionSlotIcons = List.of(EMPTY_SLOT_INGOT);

        return new SmithingTemplateItem(
            appliesTo,
            ingredients,
            upgradeDescription,
            baseSlotDescription,
            additionsSlotDescription,
            baseSlotIcons,
            additionSlotIcons
        );
    }
}
