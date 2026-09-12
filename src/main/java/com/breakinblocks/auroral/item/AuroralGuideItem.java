package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.integration.guideme.AuroralGuide;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

public class AuroralGuideItem extends Item {

    public AuroralGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            if (ModList.get().isLoaded("guideme")) {
                AuroralGuide.openGuide(player);
                return InteractionResultHolder.consume(stack);
            }
            player.displayClientMessage(Component.translatable("item.auroral.guide.missing_guideme"), true);
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.success(stack);
    }
}
