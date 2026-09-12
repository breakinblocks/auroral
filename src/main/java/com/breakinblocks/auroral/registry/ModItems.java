package com.breakinblocks.auroral.registry;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.item.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Auroral.MOD_ID);

    public static final DeferredItem<BlockItem> GLACIAL_BASIN_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.GLACIAL_BASIN);
    public static final DeferredItem<BlockItem> COLD_BREWING_STAND_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.COLD_BREWING_STAND);
    public static final DeferredItem<BlockItem> HEARTHWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.HEARTHWOOD_LOG);
    public static final DeferredItem<BlockItem> SHIMMERING_ICE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.SHIMMERING_ICE);
    public static final DeferredItem<AuroraBloomItem> AURORA_BLOOM_ITEM = ITEMS.registerItem("aurora_bloom",
        properties -> new AuroraBloomItem(ModBlocks.AURORA_BLOOM.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> AURORA_BLOOM_DECORATIVE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.AURORA_BLOOM_DECORATIVE);
    public static final DeferredItem<BlockItem> ENDER_BLOOM_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.ENDER_BLOOM);
    public static final DeferredItem<BlockItem> AURORA_LANTERN_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.AURORA_LANTERN);
    public static final DeferredItem<BlockItem> SNOW_ANGEL_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.SNOW_ANGEL);
    public static final DeferredItem<BlockItem> SHIMMER_SOIL_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.SHIMMER_SOIL);

    // Basic Materials - using registerSimpleItem for items with no custom properties
    public static final DeferredItem<Item> UNREFINED_SHIMMERSTEEL = ITEMS.registerSimpleItem("unrefined_shimmersteel");
    public static final ResourceKey<TrimMaterial> SHIMMERSTEEL_TRIM_MATERIAL =
        ResourceKey.create(Registries.TRIM_MATERIAL, Auroral.id("shimmersteel"));

    public static final DeferredItem<Item> SHIMMERSTEEL_INGOT = ITEMS.registerItem("shimmersteel_ingot",
        props -> new Item(props.trimMaterial(SHIMMERSTEEL_TRIM_MATERIAL)));
    public static final DeferredItem<Item> SHIMMERWEAVE_FABRIC = ITEMS.registerSimpleItem("shimmerweave_fabric");
    public static final DeferredItem<Item> WOVEN_LEATHER = ITEMS.registerSimpleItem("woven_leather");
    public static final DeferredItem<Item> AURORA_SHARD = ITEMS.registerSimpleItem("aurora_shard");
    public static final DeferredItem<Item> FROZEN_PETALS = ITEMS.registerSimpleItem("frozen_petals");
    public static final DeferredItem<Item> AURORA_ENDER_SHARD = ITEMS.registerSimpleItem("aurora_ender_shard");

    public static final DeferredItem<SmithingTemplateItem> SHIMMERSTEEL_UPGRADE_SMITHING_TEMPLATE = ITEMS.registerItem(
        "shimmersteel_upgrade_smithing_template",
        props -> createShimmersteelUpgradeTemplate(props));

    public static final DeferredItem<GlowLeekItem> GLOW_LEEK = ITEMS.registerItem("glow_leek",
        props -> new GlowLeekItem(props));

    public static final DeferredItem<BlockItem> GLOW_LEEK_SEEDS = ITEMS.registerSimpleBlockItem("glow_leek_seeds", ModBlocks.GLOW_LEEK);

    public static final DeferredItem<CandiedGlowLeekItem> CANDIED_GLOW_LEEK = ITEMS.registerItem("candied_glow_leek",
        props -> new CandiedGlowLeekItem(props));

    public static final DeferredItem<HotCocoaItem> HOT_COCOA = ITEMS.registerItem("hot_cocoa",
        props -> new HotCocoaItem(props));

    public static final DeferredItem<FrostedCookiesItem> FROSTED_COOKIES = ITEMS.registerItem("frosted_cookies",
        props -> new FrostedCookiesItem(props));

    public static final DeferredItem<RoastedSnowballItem> ROASTED_SNOWBALL = ITEMS.registerItem("roasted_snowball",
        props -> new RoastedSnowballItem(props));

    public static final DeferredItem<SugaredRoastedSnowballItem> SUGARED_ROASTED_SNOWBALL = ITEMS.registerItem("sugared_roasted_snowball",
        props -> new SugaredRoastedSnowballItem(props));

    public static final DeferredItem<SnoreItem> SNORE = ITEMS.registerItem("snore",
        props -> new SnoreItem(props));

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
        props -> new ShimmersteelBowItem(props.durability(384)
            .enchantable(ModToolTiers.SHIMMERSTEEL.enchantmentValue())
            .repairable(ModTags.Items.SHIMMERSTEEL_REPAIR)));

    public static final DeferredItem<ShimmersteelHoeItem> SHIMMERSTEEL_HOE = ITEMS.registerItem("shimmersteel_hoe",
        props -> new ShimmersteelHoeItem(props));

    public static final DeferredItem<ShimmerweaveGogglesItem> SHIMMERWEAVE_GOGGLES = ITEMS.registerItem("shimmerweave_goggles",
        props -> new ShimmerweaveGogglesItem(props.humanoidArmor(ModArmorMaterials.SHIMMERWEAVE, ArmorType.HELMET)));

    public static final DeferredItem<ShimmerweaveTunicItem> SHIMMERWEAVE_TUNIC = ITEMS.registerItem("shimmerweave_tunic",
        props -> new ShimmerweaveTunicItem(props.humanoidArmor(ModArmorMaterials.SHIMMERWEAVE, ArmorType.CHESTPLATE)));

    public static final DeferredItem<ShimmerweaveLeggingsItem> SHIMMERWEAVE_LEGGINGS = ITEMS.registerItem("shimmerweave_leggings",
        props -> new ShimmerweaveLeggingsItem(props.humanoidArmor(ModArmorMaterials.SHIMMERWEAVE, ArmorType.LEGGINGS)));

    public static final DeferredItem<ShimmerweaveSkatesItem> SHIMMERWEAVE_SKATES = ITEMS.registerItem("shimmerweave_skates",
        props -> new ShimmerweaveSkatesItem(props.humanoidArmor(ModArmorMaterials.SHIMMERWEAVE, ArmorType.BOOTS)));

    // Shimmersteel-tier spear. Mirrors iron_spear parameters; tier supplies the damage bonus.
    public static final DeferredItem<ShimmerSpearItem> SHIMMER_SPEAR = ITEMS.registerItem("shimmer_spear",
        props -> new ShimmerSpearItem(props.spear(
            ModToolTiers.SHIMMERSTEEL,
            0.95F, 0.95F, 0.6F, 2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F)));

    public static final DeferredItem<SpawnEggItem> AURORAL_NAUTILUS_SPAWN_EGG = ITEMS.registerItem(
        "auroral_nautilus_spawn_egg",
        properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.AURORAL_NAUTILUS.get())));

    public static final DeferredItem<SpawnEggItem> AURORAL_SNOWLETTE_SPAWN_EGG = ITEMS.registerItem(
        "auroral_snowlette_spawn_egg",
        properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.AURORAL_SNOWLETTE.get())));

    public static final DeferredItem<AuroralGuideItem> GUIDE = ITEMS.registerItem("guide",
        props -> new AuroralGuideItem(props.stacksTo(1)));

    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;

    // 26.1 moved these sprites from item/empty_{armor_,}slot_* to container/slot/*.
    private static final Identifier EMPTY_SLOT_HELMET = Identifier.withDefaultNamespace("container/slot/helmet");
    private static final Identifier EMPTY_SLOT_CHESTPLATE = Identifier.withDefaultNamespace("container/slot/chestplate");
    private static final Identifier EMPTY_SLOT_LEGGINGS = Identifier.withDefaultNamespace("container/slot/leggings");
    private static final Identifier EMPTY_SLOT_BOOTS = Identifier.withDefaultNamespace("container/slot/boots");
    private static final Identifier EMPTY_SLOT_SWORD = Identifier.withDefaultNamespace("container/slot/sword");
    private static final Identifier EMPTY_SLOT_PICKAXE = Identifier.withDefaultNamespace("container/slot/pickaxe");
    private static final Identifier EMPTY_SLOT_AXE = Identifier.withDefaultNamespace("container/slot/axe");
    private static final Identifier EMPTY_SLOT_SHOVEL = Identifier.withDefaultNamespace("container/slot/shovel");
    private static final Identifier EMPTY_SLOT_HOE = Identifier.withDefaultNamespace("container/slot/hoe");
    private static final Identifier EMPTY_SLOT_SPEAR = Identifier.withDefaultNamespace("container/slot/spear");
    private static final Identifier EMPTY_SLOT_INGOT = Identifier.withDefaultNamespace("container/slot/ingot");

    private static SmithingTemplateItem createShimmersteelUpgradeTemplate(Item.Properties properties) {
        Component appliesTo = Component.translatable(
            Util.makeDescriptionId("item", Auroral.id("smithing_template.shimmersteel_upgrade.applies_to"))
        ).withStyle(DESCRIPTION_FORMAT);

        Component ingredients = Component.translatable(
            Util.makeDescriptionId("item", Auroral.id("smithing_template.shimmersteel_upgrade.ingredients"))
        ).withStyle(DESCRIPTION_FORMAT);

        Component baseSlotDescription = Component.translatable(
            Util.makeDescriptionId("item", Auroral.id("smithing_template.shimmersteel_upgrade.base_slot_description"))
        );
        Component additionsSlotDescription = Component.translatable(
            Util.makeDescriptionId("item", Auroral.id("smithing_template.shimmersteel_upgrade.additions_slot_description"))
        );

        List<Identifier> baseSlotIcons = List.of(
            EMPTY_SLOT_HELMET,
            EMPTY_SLOT_SWORD,
            EMPTY_SLOT_CHESTPLATE,
            EMPTY_SLOT_PICKAXE,
            EMPTY_SLOT_LEGGINGS,
            EMPTY_SLOT_AXE,
            EMPTY_SLOT_BOOTS,
            EMPTY_SLOT_SHOVEL,
            EMPTY_SLOT_HOE,
            EMPTY_SLOT_SPEAR
        );

        List<Identifier> additionSlotIcons = List.of(EMPTY_SLOT_INGOT);

        return new SmithingTemplateItem(
            appliesTo,
            ingredients,
            baseSlotDescription,
            additionsSlotDescription,
            baseSlotIcons,
            additionSlotIcons,
            properties
        );
    }
}
