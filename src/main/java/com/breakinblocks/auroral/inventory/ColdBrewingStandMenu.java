package com.breakinblocks.auroral.inventory;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.block.ColdBrewingStandBlockEntity;
import com.breakinblocks.auroral.registry.ModMenuTypes;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;

import com.mojang.datafixers.util.Pair;
import java.util.Optional;

/**
 * Custom menu for Cold Brewing Stand that uses Snowballs as fuel.
 * Displays a snowball icon in the fuel slot instead of blaze powder.
 */
public class ColdBrewingStandMenu extends AbstractContainerMenu {
    /**
     * Snowball fuel slot sprite - displayed when no snowball is in the fuel slot.
     */
    public static final ResourceLocation EMPTY_SLOT_FUEL = Auroral.id("container/slot/cold_brewing_fuel");

    /**
     * Potion slot sprite - same as vanilla.
     */
    static final ResourceLocation EMPTY_SLOT_POTION = ResourceLocation.withDefaultNamespace("container/slot/potion");

    private static final int BOTTLE_SLOT_START = 0;
    private static final int BOTTLE_SLOT_END = 2;
    private static final int INGREDIENT_SLOT = 3;
    private static final int FUEL_SLOT = 4;
    private static final int SLOT_COUNT = 5;
    private static final int DATA_COUNT = 2;
    private static final int INV_SLOT_START = 5;
    private static final int INV_SLOT_END = 32;
    private static final int USE_ROW_SLOT_START = 32;
    private static final int USE_ROW_SLOT_END = 41;

    private final Container brewingStand;
    private final ContainerData brewingStandData;
    private final Slot ingredientSlot;

    /**
     * Client-side constructor.
     */
    public ColdBrewingStandMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(5), new SimpleContainerData(2));
    }

    /**
     * Server-side constructor.
     */
    public ColdBrewingStandMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenuTypes.COLD_BREWING_STAND.get(), containerId);
        checkContainerSize(container, SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.brewingStand = container;
        this.brewingStandData = data;

        PotionBrewing potionBrewing = playerInventory.player.level().potionBrewing();

        // Add potion bottle slots (0, 1, 2)
        this.addSlot(new PotionSlot(potionBrewing, container, 0, 56, 51));
        this.addSlot(new PotionSlot(potionBrewing, container, 1, 79, 58));
        this.addSlot(new PotionSlot(potionBrewing, container, 2, 102, 51));

        // Add ingredient slot (3)
        this.ingredientSlot = this.addSlot(new IngredientsSlot(potionBrewing, container, 3, 79, 17));

        // Add fuel slot (4) - uses snowball icon
        this.addSlot(new SnowballFuelSlot(container, 4, 17, 17));

        // Add data slots for syncing brew time and fuel
        this.addDataSlots(data);

        // Add player inventory slots (3x9 grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add hotbar slots
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.brewingStand.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if ((index < 0 || index > 2) && index != 3 && index != 4) {
                // From player inventory
                if (SnowballFuelSlot.mayPlaceItem(stackInSlot)) {
                    // Try fuel slot first, then ingredient slot
                    if (this.moveItemStackTo(stackInSlot, 4, 5, false)
                        || this.ingredientSlot.mayPlace(stackInSlot) && !this.moveItemStackTo(stackInSlot, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.ingredientSlot.mayPlace(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (PotionSlot.mayPlaceItem(player.level().potionBrewing(), stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= INV_SLOT_START && index < INV_SLOT_END) {
                    if (!this.moveItemStackTo(stackInSlot, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= USE_ROW_SLOT_START && index < USE_ROW_SLOT_END) {
                    if (!this.moveItemStackTo(stackInSlot, INV_SLOT_START, INV_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stackInSlot, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From brewing stand to player inventory
                if (!this.moveItemStackTo(stackInSlot, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stackInSlot, result);
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stackInSlot);
        }

        return result;
    }

    public int getFuel() {
        return this.brewingStandData.get(1);
    }

    public int getBrewingTicks() {
        return this.brewingStandData.get(0);
    }

    /**
     * Custom fuel slot that accepts Snowballs and shows a snowball icon.
     */
    static class SnowballFuelSlot extends Slot {
        public SnowballFuelSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return mayPlaceItem(stack);
        }

        /**
         * Checks if an item is valid fuel (Snowballs only).
         */
        public static boolean mayPlaceItem(ItemStack stack) {
            return ColdBrewingStandBlockEntity.isValidFuel(stack);
        }

        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_SLOT_FUEL);
        }
    }

    /**
     * Ingredient slot - accepts potion ingredients.
     */
    static class IngredientsSlot extends Slot {
        private final PotionBrewing potionBrewing;

        public IngredientsSlot(PotionBrewing potionBrewing, Container container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.potionBrewing = potionBrewing;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.potionBrewing.isIngredient(stack);
        }
    }

    /**
     * Potion slot - accepts bottles and potions.
     */
    static class PotionSlot extends Slot {
        private final PotionBrewing potionBrewing;

        public PotionSlot(PotionBrewing potionBrewing, Container container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.potionBrewing = potionBrewing;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return mayPlaceItem(this.potionBrewing, stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            Optional<Holder<Potion>> optional = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
            if (optional.isPresent() && player instanceof ServerPlayer serverPlayer) {
                net.neoforged.neoforge.event.EventHooks.onPlayerBrewedPotion(player, stack);
                CriteriaTriggers.BREWED_POTION.trigger(serverPlayer, optional.get());
            }
            super.onTake(player, stack);
        }

        public static boolean mayPlaceItem(PotionBrewing potionBrewing, ItemStack stack) {
            return potionBrewing.isInput(stack) || stack.is(Items.GLASS_BOTTLE);
        }

        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_SLOT_POTION);
        }
    }
}
