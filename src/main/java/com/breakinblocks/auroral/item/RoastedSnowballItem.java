package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.registry.ModDataAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class RoastedSnowballItem extends Item {

    public static final FoodProperties ROASTED_SNOWBALL_FOOD = new FoodProperties.Builder()
        .nutrition(1)
        .saturationModifier(0.1f)
        .alwaysEdible()
        .build();

    public RoastedSnowballItem(Properties properties) {
        super(properties.food(ROASTED_SNOWBALL_FOOD));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.auroral.roasted_snowball.tooltip.line1")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.auroral.roasted_snowball.tooltip.line2")
            .withStyle(ChatFormatting.RED));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            player.setData(ModDataAttachments.VERY_NAUGHTY, true);
            player.sendSystemMessage(
                Component.translatable("item.auroral.roasted_snowball.eaten"),
                true
            );
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
