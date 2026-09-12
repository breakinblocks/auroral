package com.breakinblocks.auroral.item;

import com.breakinblocks.auroral.block.AuroraBloomBlock;
import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.util.SnowBlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;

public class AuroraBloomItem extends BlockItem {

    public AuroraBloomItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clicked);

        if (clickedState.is(Blocks.POWDER_SNOW)) {
            return placeSnowLogged(context, clicked, false);
        }
        if (SnowBlockHelper.isSnowLayer(clickedState)) {
            return placeSnowLogged(context, clicked, true);
        }
        return super.useOn(context);
    }

    private InteractionResult placeSnowLogged(UseOnContext context, BlockPos pos, boolean snowLayer) {
        Level level = context.getLevel();
        BlockState bloom = ModBlocks.AURORA_BLOOM.get().defaultBlockState()
            .setValue(AuroraBloomBlock.SNOW_LOGGED, true)
            .setValue(AuroraBloomBlock.SNOW_LOGGED_LAYER, snowLayer)
            .setValue(AuroraBloomBlock.SNOW_LAYERS, snowLayer
                ? level.getBlockState(pos).getValue(BlockStateProperties.LAYERS) : 1);
        if (!bloom.canSurvive(level, pos) || !level.setBlock(pos, bloom, Block.UPDATE_ALL)) {
            return InteractionResult.FAIL;
        }
        Player player = context.getPlayer();
        SoundType sound = bloom.getSoundType(level, pos, player);
        level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS,
            (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, bloom));
        ItemStack stack = context.getItemInHand();
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
