package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.integration.guideme.AuroralGuide;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

public class AuroralGuideItem extends Item {

    public AuroralGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            if (ModList.get().isLoaded("guideme")) {
                AuroralGuide.openGuide(player);
                return InteractionResult.CONSUME;
            }
            player.sendOverlayMessage(Component.translatable("item.auroral.guide.missing_guideme"));
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }
}
