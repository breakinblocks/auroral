package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.registry.ModDataAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SugaredRoastedSnowballItem extends Item {

    public SugaredRoastedSnowballItem(Properties properties) {
        super(properties
            .food(RoastedSnowballItem.ROASTED_SNOWBALL_FOOD)
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, Boolean.TRUE));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.auroral.sugared_roasted_snowball.tooltip.line1")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.auroral.sugared_roasted_snowball.tooltip.line2")
            .withStyle(ChatFormatting.AQUA));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            player.setData(ModDataAttachments.VERY_NAUGHTY, false);
            player.sendSystemMessage(
                Component.translatable("item.auroral.sugared_roasted_snowball.eaten"),
                true
            );
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
