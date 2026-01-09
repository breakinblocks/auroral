package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.inventory.ColdBrewingStandMenu;
import com.breakinblocks.auroral.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the Cold Brewing Stand.
 * Functions like a vanilla brewing stand but uses Snowballs as fuel.
 *
 * Slots:
 * 0-2: Potion bottles
 * 3: Ingredient
 * 4: Fuel (Snowballs)
 */
public class ColdBrewingStandBlockEntity extends BaseContainerBlockEntity {

    private static final int INGREDIENT_SLOT = 3;
    private static final int FUEL_SLOT = 4;

    /**
     * Fuel per snowball (vanilla blaze powder gives 20).
     * Snowballs give 10 uses each (less efficient but more accessible).
     */
    public static final int FUEL_PER_SNOWBALL = 10;

    /**
     * Time to brew in ticks (same as vanilla: 400 ticks = 20 seconds).
     */
    public static final int BREW_TIME = 400;

    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int brewTime;
    private int fuel;
    private Item ingredient;

    /**
     * Container data for syncing to client (for GUI).
     */
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> brewTime;
                case 1 -> fuel;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> brewTime = value;
                case 1 -> fuel = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public ColdBrewingStandBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COLD_BREWING_STAND.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.auroral.cold_brewing_stand");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new ColdBrewingStandMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // Items are loaded by BaseContainerBlockEntity.loadAdditional
        this.brewTime = tag.getInt("BrewTime");
        this.fuel = tag.getInt("Fuel");

        // Load the ingredient item for mid-brew save/load
        if (tag.contains("Ingredient")) {
            String ingredientId = tag.getString("Ingredient");
            if (!ingredientId.isEmpty()) {
                ResourceLocation itemId = ResourceLocation.tryParse(ingredientId);
                if (itemId != null) {
                    this.ingredient = BuiltInRegistries.ITEM.get(itemId);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // Items are saved by BaseContainerBlockEntity.saveAdditional
        tag.putInt("BrewTime", this.brewTime);
        tag.putInt("Fuel", this.fuel);

        // Save the ingredient item for mid-brew save/load
        if (this.ingredient != null) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(this.ingredient);
            tag.putString("Ingredient", itemId.toString());
        }
    }

    /**
     * Checks if an item is valid fuel for the Cold Brewing Stand.
     * Uses Snowballs instead of Blaze Powder.
     */
    public static boolean isValidFuel(ItemStack stack) {
        return stack.is(Items.SNOWBALL);
    }

    /**
     * Server tick - handles brewing logic.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, ColdBrewingStandBlockEntity blockEntity) {
        ItemStack fuelStack = blockEntity.items.get(FUEL_SLOT);

        // Consume fuel if needed
        if (blockEntity.fuel <= 0 && isValidFuel(fuelStack)) {
            blockEntity.fuel = FUEL_PER_SNOWBALL;
            fuelStack.shrink(1);
            setChanged(level, pos, state);
        }

        PotionBrewing potionBrewing = level.potionBrewing();
        boolean canBrew = canBrew(potionBrewing, blockEntity.items);
        boolean isBrewing = blockEntity.brewTime > 0;
        ItemStack ingredientStack = blockEntity.items.get(INGREDIENT_SLOT);

        if (isBrewing) {
            blockEntity.brewTime--;
            boolean brewingComplete = blockEntity.brewTime == 0;

            if (brewingComplete && canBrew) {
                doBrew(level, pos, blockEntity.items, potionBrewing);
            } else if (!canBrew || !ingredientStack.is(blockEntity.ingredient)) {
                blockEntity.brewTime = 0;
            }

            setChanged(level, pos, state);
        } else if (canBrew && blockEntity.fuel > 0) {
            blockEntity.fuel--;
            blockEntity.brewTime = BREW_TIME;
            blockEntity.ingredient = ingredientStack.getItem();
            setChanged(level, pos, state);
        }

        // Update block state for bottles
        boolean[] hasBottle = new boolean[3];
        for (int i = 0; i < 3; i++) {
            hasBottle[i] = !blockEntity.items.get(i).isEmpty();
        }

        BlockState currentState = state;
        for (int i = 0; i < 3; i++) {
            if (state.getValue(ColdBrewingStandBlock.HAS_BOTTLE[i]) != hasBottle[i]) {
                currentState = currentState.setValue(ColdBrewingStandBlock.HAS_BOTTLE[i], hasBottle[i]);
            }
        }

        if (currentState != state) {
            level.setBlock(pos, currentState, 2);
        }
    }

    /**
     * Checks if brewing can proceed.
     */
    private static boolean canBrew(PotionBrewing brewing, NonNullList<ItemStack> items) {
        ItemStack ingredient = items.get(INGREDIENT_SLOT);
        if (ingredient.isEmpty()) {
            return false;
        }

        // Check if at least one bottle can be brewed
        for (int i = 0; i < 3; i++) {
            ItemStack bottle = items.get(i);
            if (!bottle.isEmpty() && brewing.hasMix(bottle, ingredient)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Performs the brewing operation.
     */
    private static void doBrew(Level level, BlockPos pos, NonNullList<ItemStack> items, PotionBrewing brewing) {
        ItemStack ingredient = items.get(INGREDIENT_SLOT);

        for (int i = 0; i < 3; i++) {
            ItemStack bottle = items.get(i);
            if (!bottle.isEmpty() && brewing.hasMix(bottle, ingredient)) {
                items.set(i, brewing.mix(ingredient, bottle));
            }
        }

        ingredient.shrink(1);

        // Fire brewing event
        level.levelEvent(1035, pos, 0);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == INGREDIENT_SLOT) {
            // Accept any potion ingredient
            return true;
        } else if (slot == FUEL_SLOT) {
            return isValidFuel(stack);
        } else {
            // Slots 0-2 are for bottles - check if it's a valid container
            return isValidBottle(stack);
        }
    }

    /**
     * Checks if an item is a valid bottle for brewing.
     */
    private static boolean isValidBottle(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION)
            || stack.is(Items.LINGERING_POTION) || stack.is(Items.GLASS_BOTTLE);
    }
}
