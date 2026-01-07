package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

/**
 * Armor materials for Auroral mod.
 */
public class ModArmorMaterials {

    /**
     * Resource key for the Shimmerweave equipment asset.
     * Links to assets/auroral/equipment/shimmerweave.json
     */
    public static final ResourceKey<EquipmentAsset> SHIMMERWEAVE_ASSET = ResourceKey.create(
        EquipmentAssets.ROOT_ID,
        Auroral.id("shimmerweave")
    );

    /**
     * Defense values for Shimmerweave armor.
     * Leather: 1, 2, 3, 1 = 7 total
     * Shimmerweave: 2, 3, 4, 2 = 11 total (between leather and chainmail)
     */
    private static final Map<ArmorType, Integer> SHIMMERWEAVE_DEFENSE = createDefenseMap();

    private static Map<ArmorType, Integer> createDefenseMap() {
        EnumMap<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.BOOTS, 2);
        map.put(ArmorType.LEGGINGS, 4);
        map.put(ArmorType.CHESTPLATE, 3);
        map.put(ArmorType.HELMET, 2);
        map.put(ArmorType.BODY, 3); // For animal armor
        return map;
    }

    /**
     * Shimmerweave Armor Material.
     * Slightly better than leather, high enchantability, lightweight.
     *
     * Durability: 12x multiplier (leather is 5, iron is 15)
     * Protection: Similar to leather but with special abilities
     * Enchantability: 22 (like gold, high for magical armor)
     */
    public static final ArmorMaterial SHIMMERWEAVE = new ArmorMaterial(
        12, // Durability multiplier
        SHIMMERWEAVE_DEFENSE,
        22, // Enchantability (high, like gold)
        SoundEvents.ARMOR_EQUIP_LEATHER, // Equip sound (fabric-like)
        0.0f, // Toughness (no extra toughness)
        0.0f, // Knockback resistance
        ModTags.Items.SHIMMERWEAVE_REPAIR, // Repair tag
        SHIMMERWEAVE_ASSET // Equipment asset key
    );
}
