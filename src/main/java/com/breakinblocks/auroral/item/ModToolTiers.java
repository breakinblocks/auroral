package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.registry.ModTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * Tool tiers for Auroral tools.
 */
public enum ModToolTiers implements Tier {

    /**
     * Shimmersteel tool tier.
     * Slightly better than Iron, with high enchantability.
     *
     * Stats compared to Iron:
     * - Iron: durability 250, speed 6.0, damage 2.0, enchantability 14
     * - Shimmersteel: durability 350, speed 6.5, damage 2.5, enchantability 22
     */
    SHIMMERSTEEL(
        BlockTags.INCORRECT_FOR_IRON_TOOL, // Same mining level as iron
        350,    // Durability (iron is 250)
        6.5f,   // Mining speed (iron is 6.0)
        2.5f,   // Attack damage bonus (iron is 2.0)
        22,     // Enchantability (iron is 14, gold is 22)
        () -> Ingredient.of(ModTags.Items.SHIMMERSTEEL_REPAIR) // Repair ingredient
    );

    private final TagKey<Block> incorrectBlocksForDrops;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;

    ModToolTiers(TagKey<Block> incorrectBlocksForDrops, int uses, float speed, float damage, int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        this.incorrectBlocksForDrops = incorrectBlocksForDrops;
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.damage;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return this.incorrectBlocksForDrops;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}
