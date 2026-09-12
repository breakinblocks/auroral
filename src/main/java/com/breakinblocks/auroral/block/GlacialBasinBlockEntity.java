package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.config.AuroralConfig;
import com.breakinblocks.auroral.registry.ModBlockEntities;
import com.breakinblocks.auroral.util.AuroraHelper;
import com.breakinblocks.auroral.util.BiomeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
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

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot != SLOT_INPUT || GlacialBasinBlock.canInfuse(stack);
        }
    };

    private final IItemHandler externalView = new RestrictedView();

    @Nullable
    private BlockCapabilityCache<IItemHandler, Direction> belowOutputCache;

    public GlacialBasinBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GLACIAL_BASIN.get(), pos, state);
    }

    public IItemHandler getItemHandler() {
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

        ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
        if (input.isEmpty()) return;

        GlacialBasinBlock.InfusionRecipe recipe = GlacialBasinBlock.getInfusionRecipe(input);
        if (recipe == null) return;

        BlockState state = getBlockState();
        int aura = state.getValue(GlacialBasinBlock.AURA_LEVEL);
        if (aura < recipe.auraCost()) return;

        ItemStack result = recipe.result();
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (!output.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(output, result)) return;
            if (output.getCount() + result.getCount() > output.getMaxStackSize()) return;
        }

        input.shrink(1);
        inventory.setStackInSlot(SLOT_INPUT, input);
        if (output.isEmpty()) {
            inventory.setStackInSlot(SLOT_OUTPUT, result.copy());
        } else {
            output.grow(result.getCount());
            inventory.setStackInSlot(SLOT_OUTPUT, output);
        }
        level.setBlock(pos, state.setValue(GlacialBasinBlock.AURA_LEVEL, aura - recipe.auraCost()), 3);
        setChanged();
    }

    private void tryEjectOutput(ServerLevel level, BlockPos pos) {
        if (++ejectTickCounter < AUTO_EJECT_INTERVAL) return;
        ejectTickCounter = 0;

        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) return;

        if (belowOutputCache == null) {
            belowOutputCache = BlockCapabilityCache.create(
                Capabilities.ItemHandler.BLOCK, level, pos.below(), Direction.UP);
        }
        IItemHandler target = belowOutputCache.getCapability();
        if (target == null) return;

        ItemStack toInsert = inventory.extractItem(SLOT_OUTPUT, output.getCount(), true);
        ItemStack remainder = ItemHandlerHelper.insertItem(target, toInsert, false);
        int inserted = toInsert.getCount() - remainder.getCount();
        if (inserted > 0) {
            inventory.extractItem(SLOT_OUTPUT, inserted, false);
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

    public void dropContents(Level level, BlockPos pos) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < SLOT_COUNT; i++) {
            stacks.set(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(level, pos, stacks);
    }

    public ItemStack takeOutput() {
        ItemStack stack = inventory.getStackInSlot(SLOT_OUTPUT);
        inventory.setStackInSlot(SLOT_OUTPUT, ItemStack.EMPTY);
        return stack;
    }

    public boolean hasOutput() {
        return !inventory.getStackInSlot(SLOT_OUTPUT).isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("FillCounter", fillTickCounter);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fillTickCounter = tag.getInt("FillCounter");
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private final class RestrictedView implements IItemHandler {
        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != SLOT_INPUT || !GlacialBasinBlock.canInfuse(stack)) {
                return stack;
            }
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != SLOT_OUTPUT) {
                return ItemStack.EMPTY;
            }
            return inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == SLOT_INPUT && GlacialBasinBlock.canInfuse(stack);
        }
    }
}
