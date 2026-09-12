package com.breakinblocks.auroral.datagen;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModItems;
import com.breakinblocks.auroral.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Data generator for item tags.
 */
public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, Auroral.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Items.SHIMMERSTEEL_REPAIR).add(ModItems.SHIMMERSTEEL_INGOT.get());
        tag(ModTags.Items.SHIMMERWEAVE_REPAIR).add(ModItems.SHIMMERWEAVE_FABRIC.get());

        // Add shimmersteel ingot to common ingots tag
        tag(Tags.Items.INGOTS)
            .add(ModItems.SHIMMERSTEEL_INGOT.get());

        tag(ItemTags.TRIM_MATERIALS)
            .add(ModItems.SHIMMERSTEEL_INGOT.get());

        // Aurora self-repair tag - items that repair during aurora events
        tag(ModTags.Items.AURORA_SELF_REPAIR)
            // Shimmersteel tools
            .add(ModItems.SHIMMERSTEEL_PICKAXE.get())
            .add(ModItems.SHIMMERSTEEL_AXE.get())
            .add(ModItems.SHIMMERSTEEL_SHOVEL.get())
            .add(ModItems.SHIMMERSTEEL_SWORD.get())
            .add(ModItems.SHIMMERSTEEL_HOE.get())
            .add(ModItems.SHIMMERSTEEL_BOW.get())
            .add(ModItems.SHIMMER_SPEAR.get())
            .add(ModItems.SHIMMERSTEEL_NAUTILUS_ARMOR.get())
            // Shimmerweave armor
            .add(ModItems.SHIMMERWEAVE_GOGGLES.get())
            .add(ModItems.SHIMMERWEAVE_TUNIC.get())
            .add(ModItems.SHIMMERWEAVE_LEGGINGS.get())
            .add(ModItems.SHIMMERWEAVE_SKATES.get());

        tag(ItemTags.SWORDS).add(ModItems.SHIMMERSTEEL_SWORD.get());
        tag(ItemTags.AXES).add(ModItems.SHIMMERSTEEL_AXE.get());
        tag(ItemTags.PICKAXES).add(ModItems.SHIMMERSTEEL_PICKAXE.get());
        tag(ItemTags.SHOVELS).add(ModItems.SHIMMERSTEEL_SHOVEL.get());
        tag(ItemTags.HOES).add(ModItems.SHIMMERSTEEL_HOE.get());

        tag(ItemTags.DURABILITY_ENCHANTABLE)
            .add(ModItems.SHIMMER_SPEAR.get())
            .add(ModItems.SHIMMERSTEEL_SWORD.get())
            .add(ModItems.SHIMMERSTEEL_AXE.get())
            .add(ModItems.SHIMMERSTEEL_PICKAXE.get())
            .add(ModItems.SHIMMERSTEEL_SHOVEL.get())
            .add(ModItems.SHIMMERSTEEL_HOE.get())
            .add(ModItems.SHIMMERSTEEL_BOW.get())
            .add(ModItems.SHIMMERWEAVE_GOGGLES.get())
            .add(ModItems.SHIMMERWEAVE_TUNIC.get())
            .add(ModItems.SHIMMERWEAVE_LEGGINGS.get())
            .add(ModItems.SHIMMERWEAVE_SKATES.get());

        tag(ItemTags.VANISHING_ENCHANTABLE)
            .add(ModItems.SHIMMER_SPEAR.get())
            .add(ModItems.SHIMMERSTEEL_SWORD.get())
            .add(ModItems.SHIMMERSTEEL_AXE.get())
            .add(ModItems.SHIMMERSTEEL_PICKAXE.get())
            .add(ModItems.SHIMMERSTEEL_SHOVEL.get())
            .add(ModItems.SHIMMERSTEEL_HOE.get())
            .add(ModItems.SHIMMERSTEEL_BOW.get())
            .add(ModItems.SHIMMERWEAVE_GOGGLES.get())
            .add(ModItems.SHIMMERWEAVE_TUNIC.get())
            .add(ModItems.SHIMMERWEAVE_LEGGINGS.get())
            .add(ModItems.SHIMMERWEAVE_SKATES.get());

        tag(ItemTags.SWORD_ENCHANTABLE).add(ModItems.SHIMMERSTEEL_SWORD.get());
        tag(ItemTags.SHARP_WEAPON_ENCHANTABLE)
            .add(ModItems.SHIMMERSTEEL_SWORD.get())
            .add(ModItems.SHIMMERSTEEL_AXE.get())
            .add(ModItems.SHIMMER_SPEAR.get());
        tag(ItemTags.WEAPON_ENCHANTABLE)
            .add(ModItems.SHIMMERSTEEL_SWORD.get())
            .add(ModItems.SHIMMERSTEEL_AXE.get())
            .add(ModItems.SHIMMER_SPEAR.get());
        tag(ItemTags.FIRE_ASPECT_ENCHANTABLE)
            .add(ModItems.SHIMMERSTEEL_SWORD.get())
            .add(ModItems.SHIMMERSTEEL_AXE.get());

        tag(ItemTags.MINING_ENCHANTABLE)
            .add(ModItems.SHIMMERSTEEL_PICKAXE.get())
            .add(ModItems.SHIMMERSTEEL_AXE.get())
            .add(ModItems.SHIMMERSTEEL_SHOVEL.get())
            .add(ModItems.SHIMMERSTEEL_HOE.get());
        tag(ItemTags.MINING_LOOT_ENCHANTABLE)
            .add(ModItems.SHIMMERSTEEL_PICKAXE.get())
            .add(ModItems.SHIMMERSTEEL_AXE.get())
            .add(ModItems.SHIMMERSTEEL_SHOVEL.get());

        tag(ItemTags.BOW_ENCHANTABLE).add(ModItems.SHIMMERSTEEL_BOW.get());

        tag(ItemTags.ARMOR_ENCHANTABLE)
            .add(ModItems.SHIMMERWEAVE_GOGGLES.get())
            .add(ModItems.SHIMMERWEAVE_TUNIC.get())
            .add(ModItems.SHIMMERWEAVE_LEGGINGS.get())
            .add(ModItems.SHIMMERWEAVE_SKATES.get());
        tag(ItemTags.HEAD_ARMOR_ENCHANTABLE).add(ModItems.SHIMMERWEAVE_GOGGLES.get());
        tag(ItemTags.CHEST_ARMOR_ENCHANTABLE).add(ModItems.SHIMMERWEAVE_TUNIC.get());
        tag(ItemTags.LEG_ARMOR_ENCHANTABLE).add(ModItems.SHIMMERWEAVE_LEGGINGS.get());
        tag(ItemTags.FOOT_ARMOR_ENCHANTABLE).add(ModItems.SHIMMERWEAVE_SKATES.get());
        tag(ItemTags.EQUIPPABLE_ENCHANTABLE)
            .add(ModItems.SHIMMERWEAVE_GOGGLES.get())
            .add(ModItems.SHIMMERWEAVE_TUNIC.get())
            .add(ModItems.SHIMMERWEAVE_LEGGINGS.get())
            .add(ModItems.SHIMMERWEAVE_SKATES.get());
    }
}
