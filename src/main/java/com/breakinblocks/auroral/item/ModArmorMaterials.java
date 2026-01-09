package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModTags;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Armor materials for Auroral mod.
 */
public class ModArmorMaterials {

    /**
     * Defense values for Shimmerweave armor.
     * Diamond: 3, 6, 8, 3 = 20 total
     * Shimmerweave: 3, 6, 8, 3 = 20 total (equivalent to diamond)
     */
    private static final Map<ArmorItem.Type, Integer> SHIMMERWEAVE_DEFENSE = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 3);
        map.put(ArmorItem.Type.LEGGINGS, 6);
        map.put(ArmorItem.Type.CHESTPLATE, 8);
        map.put(ArmorItem.Type.HELMET, 3);
    });

    /**
     * Shimmerweave Armor Material.
     * Diamond-equivalent protection with high enchantability.
     *
     * Durability: 26x multiplier (20% less than diamond's 33x)
     * Protection: Diamond equivalent (20 total armor points)
     * Toughness: 2.0 (same as diamond)
     * Enchantability: 22 (like gold, high for magical armor)
     */
    public static final Holder<ArmorMaterial> SHIMMERWEAVE = register(
        "shimmerweave",
        SHIMMERWEAVE_DEFENSE,
        22, // Enchantability (high, like gold)
        SoundEvents.ARMOR_EQUIP_LEATHER, // Equip sound (fabric-like)
        2.0f, // Toughness (same as diamond)
        0.0f, // Knockback resistance
        () -> Ingredient.of(ModTags.Items.SHIMMERWEAVE_REPAIR)
    );

    private static Holder<ArmorMaterial> register(
            String name,
            Map<ArmorItem.Type, Integer> defense,
            int enchantability,
            Holder<net.minecraft.sounds.SoundEvent> equipSound,
            float toughness,
            float knockbackResistance,
            Supplier<Ingredient> repairIngredient
    ) {
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(Auroral.id(name)));

        EnumMap<ArmorItem.Type, Integer> defenseMap = new EnumMap<>(ArmorItem.Type.class);
        defenseMap.putAll(defense);

        return Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            Auroral.id(name),
            new ArmorMaterial(defenseMap, enchantability, equipSound, repairIngredient, layers, toughness, knockbackResistance)
        );
    }

    /**
     * Call this method to ensure the armor materials are registered.
     */
    public static void init() {
        // Static initialization ensures the holders are created
    }
}
