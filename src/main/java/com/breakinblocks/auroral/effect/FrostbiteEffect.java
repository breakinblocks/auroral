package com.breakinblocks.auroral.effect;

import com.breakinblocks.auroral.Auroral;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Frostbite is a debilitating cold effect with the following mechanics:
 *
 * <ul>
 *   <li><b>Movement Slow:</b> Reduces movement speed by 15% per level (similar to Slowness I)</li>
 *   <li><b>Healing Reduction:</b> Reduces effectiveness of all healing by 25% per level</li>
 *   <li><b>Shield Brittleness:</b> Shields take 50% more durability damage per level</li>
 *   <li><b>Armor Weakness:</b> Reduces armor effectiveness by 2 points per level</li>
 * </ul>
 *
 * These effects are applied through event handlers in {@link com.breakinblocks.auroral.events.FrostbiteEventHandler}.
 */
public class FrostbiteEffect extends MobEffect {

    /**
     * Movement speed reduction per level (0.15 = 15% reduction per level, similar to Slowness I).
     */
    public static final double MOVEMENT_REDUCTION_PER_LEVEL = -0.15;

    /**
     * Healing reduction multiplier per level (0.25 = 25% reduction per level).
     * At level 1: 75% healing, level 2: 50% healing, level 3: 25% healing, level 4: no healing
     */
    public static final float HEALING_REDUCTION_PER_LEVEL = 0.25f;

    /**
     * Shield durability damage multiplier per level (0.5 = 50% extra damage per level).
     */
    public static final float SHIELD_BRITTLENESS_PER_LEVEL = 0.5f;

    /**
     * Armor reduction per level in armor points.
     */
    public static final double ARMOR_REDUCTION_PER_LEVEL = 2.0;

    private static final Identifier ARMOR_MODIFIER_ID = Auroral.id("frostbite_armor_reduction");
    private static final Identifier SPEED_MODIFIER_ID = Auroral.id("frostbite_speed_reduction");

    public FrostbiteEffect(MobEffectCategory category, int color) {
        super(category, color);

        // Apply armor reduction as an attribute modifier
        // This automatically scales with effect level
        addAttributeModifier(
            Attributes.ARMOR,
            ARMOR_MODIFIER_ID,
            -ARMOR_REDUCTION_PER_LEVEL,
            AttributeModifier.Operation.ADD_VALUE
        );

        // Apply movement speed reduction (similar to Slowness I)
        addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            SPEED_MODIFIER_ID,
            MOVEMENT_REDUCTION_PER_LEVEL,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    /**
     * Calculates the healing multiplier for a given effect amplifier.
     * @param amplifier The effect amplifier (0 = level 1, 1 = level 2, etc.)
     * @return The multiplier to apply to healing (1.0 = full healing, 0.0 = no healing)
     */
    public static float getHealingMultiplier(int amplifier) {
        float reduction = HEALING_REDUCTION_PER_LEVEL * (amplifier + 1);
        return Math.max(0.0f, 1.0f - reduction);
    }

    /**
     * Calculates the shield damage multiplier for a given effect amplifier.
     * @param amplifier The effect amplifier (0 = level 1, 1 = level 2, etc.)
     * @return The multiplier to apply to shield durability damage (1.0 = normal, 1.5 = 50% more, etc.)
     */
    public static float getShieldDamageMultiplier(int amplifier) {
        return 1.0f + (SHIELD_BRITTLENESS_PER_LEVEL * (amplifier + 1));
    }
}
