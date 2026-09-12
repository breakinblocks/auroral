package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.config.AuroralConfig;
import com.breakinblocks.auroral.registry.ModBlockEntities;
import com.breakinblocks.auroral.util.AuroraHelper;
import com.breakinblocks.auroral.util.BiomeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class GlacialBasinBlockEntity extends BlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_COUNT = 2;

    private static final int AUTO_PROCESS_INTERVAL = 10;
    private static final int AUTO_EJECT_INTERVAL = 10;

    private int fillTickCounter = 0;
    private int processTickCounter = 0;
    private int ejectTickCounter = 0;

    private final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    };

    /** Capability view: insert only into the input slot, extract only from the output slot. */
    private final ResourceHandler<ItemResource> externalView = new RestrictedView();

    @Nullable
    private BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> belowOutputCache;

    public GlacialBasinBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GLACIAL_BASIN.get(), pos, state);
    }

    public ResourceHandler<ItemResource> getItemHandler() {
        return externalView;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GlacialBasinBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        be.tryFillAura(serverLevel, pos, state);
        be.tryProcessInfusion(serverLevel, pos);
        be.tryEjectOutput(serverLevel, pos);
    }

    private void tryFillAura(ServerLevel level, BlockPos pos, BlockState state) {
        if (!AuroraHelper.isAuroraActive(level)) return;
        if (!BiomeHelper.isColdBiome(level, pos)) return;

        int maxAura = AuroralConfig.SERVER.basinMaxAura.get();
        int currentAura = state.getValue(GlacialBasinBlock.AURA_LEVEL);
        if (currentAura >= maxAura) return;

        fillTickCounter++;
        int fillRate = AuroralConfig.SERVER.basinFillRate.get();
        if (fillTickCounter >= fillRate) {
            fillTickCounter = 0;
            level.setBlock(pos, state.setValue(GlacialBasinBlock.AURA_LEVEL,
                Math.min(currentAura + 1, maxAura)), 3);
            setChanged();
        }
    }

    private void tryProcessInfusion(ServerLevel level, BlockPos pos) {
        if (++processTickCounter < AUTO_PROCESS_INTERVAL) return;
        processTickCounter = 0;

        ItemResource inputRes = inventory.getResource(SLOT_INPUT);
        int inputAmt = inventory.getAmountAsInt(SLOT_INPUT);
        if (inputRes.isEmpty() || inputAmt < 1) return;

        GlacialBasinBlock.InfusionRecipe recipe = GlacialBasinBlock.getInfusionRecipe(inputRes.toStack(1));
        if (recipe == null) return;

        BlockState state = getBlockState();
        int aura = state.getValue(GlacialBasinBlock.AURA_LEVEL);
        if (aura < recipe.auraCost()) return;

        ItemStack result = recipe.result();
        ItemResource outputRes = inventory.getResource(SLOT_OUTPUT);
        int outputAmt = inventory.getAmountAsInt(SLOT_OUTPUT);
        ItemResource resultRes = ItemResource.of(result);

        if (!outputRes.isEmpty()) {
            if (!outputRes.matches(result)) return;
            if (outputAmt + result.getCount() > resultRes.getMaxStackSize()) return;
        }

        inventory.set(SLOT_INPUT, inputRes, inputAmt - 1);
        if (outputRes.isEmpty()) {
            inventory.set(SLOT_OUTPUT, resultRes, result.getCount());
        } else {
            inventory.set(SLOT_OUTPUT, outputRes, outputAmt + result.getCount());
        }
        level.setBlock(pos, state.setValue(GlacialBasinBlock.AURA_LEVEL, aura - recipe.auraCost()), 3);
        setChanged();
    }

    private void tryEjectOutput(ServerLevel level, BlockPos pos) {
        if (++ejectTickCounter < AUTO_EJECT_INTERVAL) return;
        ejectTickCounter = 0;

        ItemResource outputRes = inventory.getResource(SLOT_OUTPUT);
        if (outputRes.isEmpty()) return;
        int amount = inventory.getAmountAsInt(SLOT_OUTPUT);

        if (belowOutputCache == null) {
            belowOutputCache = BlockCapabilityCache.create(
                Capabilities.Item.BLOCK, level, pos.below(), Direction.UP);
        }
        ResourceHandler<ItemResource> target = belowOutputCache.getCapability();
        if (target == null) return;

        try (Transaction tx = Transaction.openRoot()) {
            int extracted = inventory.extract(SLOT_OUTPUT, outputRes, amount, tx);
            int inserted = target.insert(outputRes, extracted, tx);
            if (inserted == 0) return;
            if (inserted < extracted) {
                inventory.insert(SLOT_OUTPUT, outputRes, extracted - inserted, tx);
            }
            tx.commit();
        }
    }

    public int getAuraLevel() {
        BlockState state = getBlockState();
        return state.hasProperty(GlacialBasinBlock.AURA_LEVEL)
            ? state.getValue(GlacialBasinBlock.AURA_LEVEL) : 0;
    }

    public void setAuraLevel(int newLevel) {
        if (level == null) return;
        BlockState state = getBlockState();
        level.setBlock(worldPosition, state.setValue(GlacialBasinBlock.AURA_LEVEL, newLevel), 3);
        setChanged();
    }

    public boolean hasAura() {
        return getAuraLevel() > 0;
    }

    public void consumeAura() {
        int currentLevel = getAuraLevel();
        if (currentLevel > 0) {
            setAuraLevel(currentLevel - 1);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, pos, inventory.copyToList());
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                inventory.set(slot, ItemResource.EMPTY, 0);
            }
        }
    }

    public ItemStack takeOutput() {
        ItemResource resource = inventory.getResource(SLOT_OUTPUT);
        if (resource.isEmpty()) return ItemStack.EMPTY;
        int amount = inventory.getAmountAsInt(SLOT_OUTPUT);
        ItemStack stack = resource.toStack(amount);
        inventory.set(SLOT_OUTPUT, ItemResource.EMPTY, 0);
        return stack;
    }

    public boolean hasOutput() {
        return !inventory.getResource(SLOT_OUTPUT).isEmpty();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("FillCounter", fillTickCounter);
        inventory.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fillTickCounter = input.getIntOr("FillCounter", 0);
        inventory.deserialize(input);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private final class RestrictedView implements ResourceHandler<ItemResource> {
        @Override public int size() { return inventory.size(); }
        @Override public ItemResource getResource(int index) { return inventory.getResource(index); }
        @Override public long getAmountAsLong(int index) { return inventory.getAmountAsLong(index); }
        @Override public long getCapacityAsLong(int index, ItemResource resource) { return inventory.getCapacityAsLong(index, resource); }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return index == SLOT_INPUT && GlacialBasinBlock.canInfuse(resource.toStack(1));
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index != SLOT_INPUT) return 0;
            if (!GlacialBasinBlock.canInfuse(resource.toStack(1))) return 0;
            return inventory.insert(index, resource, amount, transaction);
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index != SLOT_OUTPUT) return 0;
            return inventory.extract(index, resource, amount, transaction);
        }
    }
}
