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
     * Diamond: 3, 6, 8, 3 = 20 total
     * Shimmerweave: 3, 6, 8, 3 = 20 total (equivalent to diamond)
     */
    private static final Map<ArmorType, Integer> SHIMMERWEAVE_DEFENSE = createDefenseMap();

    private static Map<ArmorType, Integer> createDefenseMap() {
        EnumMap<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.BOOTS, 3);
        map.put(ArmorType.LEGGINGS, 6);
        map.put(ArmorType.CHESTPLATE, 8);
        map.put(ArmorType.HELMET, 3);
        map.put(ArmorType.BODY, 11); // For animal armor (diamond horse armor equivalent)
        return map;
    }

    /**
     * Shimmerweave Armor Material.
     * Diamond-equivalent protection with high enchantability.
     *
     * Durability: 26x multiplier (20% less than diamond's 33x)
     * Protection: Diamond equivalent (20 total armor points)
     * Toughness: 2.0 (same as diamond)
     * Enchantability: 22 (like gold, high for magical armor)
     */
    public static final ArmorMaterial SHIMMERWEAVE = new ArmorMaterial(
        26, // Durability multiplier (20% less than diamond's 33x)
        SHIMMERWEAVE_DEFENSE,
        22, // Enchantability (high, like gold)
        SoundEvents.ARMOR_EQUIP_LEATHER, // Equip sound (fabric-like)
        2.0f, // Toughness (same as diamond)
        0.0f, // Knockback resistance
        ModTags.Items.SHIMMERWEAVE_REPAIR, // Repair tag
        SHIMMERWEAVE_ASSET // Equipment asset key
    );
}
