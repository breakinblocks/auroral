package com.breakinblocks.auroral.block;

import com.breakinblocks.auroral.inventory.ColdBrewingStandMenu;
import com.breakinblocks.auroral.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ColdBrewingStandBlockEntity extends BaseContainerBlockEntity {

    private static final int INGREDIENT_SLOT = 3;
    private static final int FUEL_SLOT = 4;
    public static final int FUEL_PER_SNOWBALL = 10;
    public static final int BREW_TIME = 400;

    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int brewTime;
    private int fuel;
    private Item ingredient;

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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.brewTime = input.getIntOr("BrewTime", 0);
        this.fuel = input.getIntOr("Fuel", 0);

        String ingredientId = input.getStringOr("Ingredient", "");
        if (!ingredientId.isEmpty()) {
            Identifier itemId = Identifier.tryParse(ingredientId);
            if (itemId != null) {
                this.ingredient = BuiltInRegistries.ITEM.getValue(itemId);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("BrewTime", this.brewTime);
        output.putInt("Fuel", this.fuel);

        if (this.ingredient != null) {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(this.ingredient);
            output.putString("Ingredient", itemId.toString());
        }
    }

    public static boolean isValidFuel(ItemStack stack) {
        return stack.is(Items.SNOWBALL);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ColdBrewingStandBlockEntity blockEntity) {
        ItemStack fuelStack = blockEntity.items.get(FUEL_SLOT);

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

    private static boolean canBrew(PotionBrewing brewing, NonNullList<ItemStack> items) {
        ItemStack ingredient = items.get(INGREDIENT_SLOT);
        if (ingredient.isEmpty()) {
            return false;
        }

        for (int i = 0; i < 3; i++) {
            ItemStack bottle = items.get(i);
            if (!bottle.isEmpty() && brewing.hasMix(bottle, ingredient)) {
                return true;
            }
        }

        return false;
    }

    private static void doBrew(Level level, BlockPos pos, NonNullList<ItemStack> items, PotionBrewing brewing) {
        ItemStack ingredient = items.get(INGREDIENT_SLOT);

        for (int i = 0; i < 3; i++) {
            ItemStack bottle = items.get(i);
            if (!bottle.isEmpty() && brewing.hasMix(bottle, ingredient)) {
                items.set(i, brewing.mix(ingredient, bottle));
            }
        }

        ingredient.shrink(1);
        level.levelEvent(1035, pos, 0);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == INGREDIENT_SLOT) {
            PotionBrewing potionBrewing = this.level != null ? this.level.potionBrewing() : PotionBrewing.EMPTY;
            return potionBrewing.isIngredient(stack);
        } else if (slot == FUEL_SLOT) {
            return isValidFuel(stack);
        } else {
            return isValidBottle(stack) && this.getItem(slot).isEmpty();
        }
    }

    private static boolean isValidBottle(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION)
            || stack.is(Items.LINGERING_POTION) || stack.is(Items.GLASS_BOTTLE);
    }
}
