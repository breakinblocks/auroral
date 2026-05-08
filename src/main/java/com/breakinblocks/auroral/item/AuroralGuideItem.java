package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.integration.guideme.AuroralGuide;
import guideme.GuidesCommon;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AuroralGuideItem extends Item {

    public AuroralGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            GuidesCommon.openGuide(player, AuroralGuide.GUIDE_ID);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.success(stack);
    }
}
