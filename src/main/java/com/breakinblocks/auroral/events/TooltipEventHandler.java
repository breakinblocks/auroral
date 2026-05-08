package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@EventBusSubscriber(modid = Auroral.MOD_ID)
public class TooltipEventHandler {

    private record TooltipEntry(String translationKey, ChatFormatting style) {}

    private static Map<Item, List<TooltipEntry>> tooltipsByItem;

    private static Map<Item, List<TooltipEntry>> buildTooltipMap() {
        Map<Item, List<TooltipEntry>> map = new HashMap<>();
        Binder b = new Binder(map);

        b.ability(ModItems.SHIMMERSTEEL_PICKAXE::get, "tooltip.auroral.shimmersteel_pickaxe.gem_fortune");
        b.ability(ModItems.SHIMMERSTEEL_AXE::get, "tooltip.auroral.shimmersteel_axe.force_oxidize");
        b.ability(ModItems.SHIMMERSTEEL_SHOVEL::get, "tooltip.auroral.shimmersteel_shovel.silk_touch");
        b.ability(ModItems.SHIMMERSTEEL_HOE::get, "tooltip.auroral.shimmersteel_hoe.silk_touch");
        b.ability(ModItems.SHIMMERSTEEL_HOE::get, "tooltip.auroral.shimmersteel_hoe.shimmer_soil");
        b.ability(ModItems.SHIMMERSTEEL_SWORD::get, "tooltip.auroral.shimmersteel_sword.frostbite");
        b.ability(ModItems.SHIMMERSTEEL_SWORD::get, "tooltip.auroral.shimmersteel_sword.execute");
        b.ability(ModItems.SHIMMERSTEEL_BOW::get, "tooltip.auroral.shimmersteel_bow.star_shot");

        b.ability(ModItems.SHIMMERWEAVE_GOGGLES::get, "tooltip.auroral.shimmerweave_goggles.glow_hostiles");
        b.ability(ModItems.SHIMMERWEAVE_TUNIC::get, "tooltip.auroral.shimmerweave_tunic.extinguish");
        b.ability(ModItems.SHIMMERWEAVE_LEGGINGS::get, "tooltip.auroral.shimmerweave_leggings.snow_speed");
        b.ability(ModItems.SHIMMERWEAVE_LEGGINGS::get, "tooltip.auroral.shimmerweave_leggings.soul_speed");
        b.ability(ModItems.SHIMMERWEAVE_SKATES::get, "tooltip.auroral.shimmerweave_skates.ice_speed");
        b.ability(ModItems.SHIMMERWEAVE_SKATES::get, "tooltip.auroral.shimmerweave_skates.frost_walker");
        b.ability(ModItems.SHIMMERWEAVE_SKATES::get, "tooltip.auroral.shimmerweave_skates.lava_to_obsidian");
        b.ability(ModItems.SHIMMERWEAVE_SKATES::get, "tooltip.auroral.shimmerweave_skates.no_ice_fall");

        b.info(ModBlocks.HEARTHWOOD_LOG::asItem, "tooltip.auroral.hearthwood_log.burn_time");
        b.ability(ModBlocks.HEARTHWOOD_LOG::asItem, "tooltip.auroral.hearthwood_log.frostbite_immunity");
        b.ability(ModBlocks.HEARTHWOOD_LOG::asItem, "tooltip.auroral.hearthwood_log.phantom_ward");
        b.ability(ModBlocks.HEARTHWOOD_LOG::asItem, "tooltip.auroral.hearthwood_log.aurora_catalyst");

        b.info(ModBlocks.AURORA_LANTERN::asItem, "tooltip.auroral.aurora_lantern.decorative");

        b.info(ModBlocks.SHIMMERING_ICE::asItem, "tooltip.auroral.shimmering_ice.never_melts");
        b.info(ModBlocks.SHIMMERING_ICE::asItem, "tooltip.auroral.shimmering_ice.hydrates");
        b.info(ModBlocks.SHIMMERING_ICE::asItem, "tooltip.auroral.shimmering_ice.glow_leek");

        b.info(ModBlocks.GLACIAL_BASIN::asItem, "tooltip.auroral.glacial_basin.collect_aura");
        b.info(ModBlocks.GLACIAL_BASIN::asItem, "tooltip.auroral.glacial_basin.infuse");

        b.info(ModBlocks.COLD_BREWING_STAND::asItem, "tooltip.auroral.cold_brewing_stand.no_nether");

        b.info(ModBlocks.SNOW_ANGEL::asItem, "tooltip.auroral.snow_angel.imprint");
        b.info(ModBlocks.SNOW_ANGEL::asItem, "tooltip.auroral.snow_angel.fades");

        b.info(ModBlocks.SHIMMER_SOIL::asItem, "tooltip.auroral.shimmer_soil.growth_boost");
        b.info(ModBlocks.SHIMMER_SOIL::asItem, "tooltip.auroral.shimmer_soil.tilled");

        b.info(ModBlocks.AURORA_BLOOM::asItem, "tooltip.auroral.aurora_bloom.grows_at_night");
        b.info(ModBlocks.AURORA_BLOOM::asItem, "tooltip.auroral.aurora_bloom.frozen_petals");

        b.info(ModBlocks.ENDER_BLOOM::asItem, "tooltip.auroral.ender_bloom.ender_shards");

        b.info(ModBlocks.GLOW_LEEK::asItem, "tooltip.auroral.glow_leek.grows_on_ice");
        b.ability(ModBlocks.GLOW_LEEK::asItem, "tooltip.auroral.glow_leek.night_vision");

        b.ability(ModItems.FROZEN_PETALS::get, "tooltip.auroral.frozen_petals.preserve_snow_angel");
        b.ability(ModItems.AURORA_ENDER_SHARD::get, "tooltip.auroral.aurora_ender_shard.ender_bloom");

        return map;
    }

    private static Map<Item, List<TooltipEntry>> getMap() {
        Map<Item, List<TooltipEntry>> local = tooltipsByItem;
        if (local == null) {
            local = buildTooltipMap();
            tooltipsByItem = local;
        }
        return local;
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<TooltipEntry> entries = getMap().get(stack.getItem());
        if (entries == null) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        for (TooltipEntry entry : entries) {
            tooltip.add(Component.translatable(entry.translationKey).withStyle(entry.style));
        }
    }

    private record Binder(Map<Item, List<TooltipEntry>> map) {
        void ability(Supplier<? extends Item> item, String key) {
            map.computeIfAbsent(item.get(), k -> new ArrayList<>())
                .add(new TooltipEntry(key, ChatFormatting.BLUE));
        }
        void info(Supplier<? extends Item> item, String key) {
            map.computeIfAbsent(item.get(), k -> new ArrayList<>())
                .add(new TooltipEntry(key, ChatFormatting.GRAY));
        }
    }
}
