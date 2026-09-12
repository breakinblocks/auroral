package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.registry.ModBlocks;
import com.breakinblocks.auroral.util.AuroraHelper;
import com.breakinblocks.auroral.util.SnowBlockHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AuroraBloomBlock extends BushBlock implements BonemealableBlock {

    public static final MapCodec<AuroraBloomBlock> CODEC = simpleCodec(AuroraBloomBlock::new);

    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final BooleanProperty SNOW_LOGGED = BooleanProperty.create("snow_logged");
    /** True when snow-logging refers to a snow layer rather than powder snow. */
    public static final BooleanProperty SNOW_LOGGED_LAYER = BooleanProperty.create("snow_logged_layer");
    public static final IntegerProperty SNOW_LAYERS = IntegerProperty.create("snow_layers", 1, 8);

    // Shapes for each growth stage
    private static final VoxelShape[] SHAPES = new VoxelShape[] {
        Block.box(5.0, 0.0, 5.0, 11.0, 4.0, 11.0),   // Stage 0 - tiny sprout
        Block.box(4.0, 0.0, 4.0, 12.0, 7.0, 12.0),   // Stage 1 - small
        Block.box(3.0, 0.0, 3.0, 13.0, 10.0, 13.0),  // Stage 2 - medium
        Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0)   // Stage 3 - full bloom
    };

    public AuroraBloomBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(AGE, 0)
            .setValue(SNOW_LOGGED, false)
            .setValue(SNOW_LOGGED_LAYER, false)
            .setValue(SNOW_LAYERS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, SNOW_LOGGED, SNOW_LOGGED_LAYER, SNOW_LAYERS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState clicked = context.getLevel().getBlockState(context.getClickedPos());
        return withSnow(defaultBlockState(), clicked);
    }

    public static BlockState withSnow(BlockState bloom, BlockState snow) {
        boolean layer = SnowBlockHelper.isSnowLayer(snow);
        return bloom.setValue(SNOW_LOGGED, layer || snow.is(Blocks.POWDER_SNOW))
            .setValue(SNOW_LOGGED_LAYER, layer)
            .setValue(SNOW_LAYERS, layer ? snow.getValue(SnowLayerBlock.LAYERS) : 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<BushBlock> codec() {
        return (MapCodec<BushBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SNOW) ||
               state.is(Blocks.SNOW_BLOCK) ||
               state.is(Blocks.POWDER_SNOW) ||
               state.is(Blocks.ICE) ||
               state.is(Blocks.PACKED_ICE) ||
               state.is(Blocks.BLUE_ICE) ||
               state.getBlock() instanceof ShimmeringIceBlock ||
               state.isFaceSturdy(level, pos, net.minecraft.core.Direction.UP);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(SNOW_LOGGED)) {
            return true;
        }
        BlockPos below = pos.below();
        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }

    public boolean isMaxAge(BlockState state) {
        return state.getValue(AGE) >= MAX_AGE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            decay(state, level, pos);
            return;
        }

        if (!AuroraHelper.isNightTime(level)) {
            level.sendParticles(ParticleTypes.SNOWFLAKE,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                5, 0.3, 0.3, 0.3, 0.01);
            decay(state, level, pos);
            return;
        }

        if (!isMaxAge(state) && random.nextFloat() < 0.05f) {
            int newAge = state.getValue(AGE) + 1;
            level.setBlock(pos, state.setValue(AGE, newAge), 2);
        }
    }

    private void decay(BlockState state, ServerLevel level, BlockPos pos) {
        if (state.getValue(SNOW_LOGGED)) {
            level.setBlock(pos, restoredSnowState(state), Block.UPDATE_ALL);
        } else {
            level.destroyBlock(pos, false);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        restoreSnowAfterRemoval(state, level, pos);
    }

    public static void restoreSnowAfterRemoval(BlockState state, ServerLevel level, BlockPos pos) {
        // Defer the snow restore: setBlock here would clobber the outer chunk.setBlockState's
        // post-apply check (newBlock != section's block => returns null), which cancels the
        // removal and skips playerDestroy's drops.
        if (state.getValue(SNOW_LOGGED)) {
            BlockState snow = restoredSnowState(state);
            MinecraftServer server = level.getServer();
            if (server != null) {
                server.schedule(new TickTask(server.getTickCount() + 1, () -> {
                    if (level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, snow, Block.UPDATE_ALL);
                    }
                }));
            }
        }
    }

    public static BlockState restoredSnowState(BlockState state) {
        return state.getValue(SNOW_LOGGED_LAYER)
            ? Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, state.getValue(SNOW_LAYERS))
            : Blocks.POWDER_SNOW.defaultBlockState();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);

        if (random.nextInt(6 - age) == 0) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double y = pos.getY() + 0.1 + (age * 0.15) + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;

            level.addParticle(ParticleTypes.END_ROD, x, y, z,
                (random.nextDouble() - 0.5) * 0.02,
                random.nextDouble() * 0.02,
                (random.nextDouble() - 0.5) * 0.02);
        }

        if (age >= 2 && random.nextInt(10) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.5 + (age * 0.2);
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0, -0.02, 0);
        }

        // Marker for snow-logged blooms so they can be spotted if buried under snow above.
        if (state.getValue(SNOW_LOGGED) && random.nextInt(40) == 0) {
            double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
            double y = pos.getY() + 1.05 + random.nextDouble() * 0.4;
            double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0, 0.015, 0.0);
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        // Light increases with age: 3, 5, 6, 8
        return 3 + (state.getValue(AGE) * 2) - (state.getValue(AGE) > 2 ? 1 : 0);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.ENDER_PEARL)) {
            if (!level.isClientSide()) {
                int age = state.getValue(AGE);
                BlockState newState = ModBlocks.ENDER_BLOOM.get().defaultBlockState()
                    .setValue(EnderBloomBlock.AGE, age)
                    .setValue(SNOW_LOGGED, state.getValue(SNOW_LOGGED))
                    .setValue(SNOW_LOGGED_LAYER, state.getValue(SNOW_LOGGED_LAYER))
                    .setValue(SNOW_LAYERS, state.getValue(SNOW_LAYERS));
                level.setBlock(pos, newState, Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0f, 1.0f);
                ((ServerLevel) level).sendParticles(ParticleTypes.PORTAL,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    20, 0.3, 0.3, 0.3, 0.0);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !isMaxAge(state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        // 75% chance of success
        return random.nextFloat() < 0.75f;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int newAge = Math.min(MAX_AGE, state.getValue(AGE) + 1);
        level.setBlock(pos, state.setValue(AGE, newAge), 2);
    }
}
