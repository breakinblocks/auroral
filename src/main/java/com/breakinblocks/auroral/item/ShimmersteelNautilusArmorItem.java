package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class ShimmersteelNautilusArmorItem extends Item {

    public static final int DEFENSE = 7;
    public static final float TOUGHNESS = 1.0F;

    public ShimmersteelNautilusArmorItem(Properties properties) {
        super(properties.stacksTo(1).attributes(createAttributes()));
    }

    private static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ARMOR,
                new AttributeModifier(Auroral.id("nautilus_armor"), DEFENSE, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.BODY)
            .add(Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(Auroral.id("nautilus_armor"), TOUGHNESS, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.BODY)
            .build();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(ModItems.SHIMMERSTEEL_INGOT.get());
    }
}
