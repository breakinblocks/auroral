package com.breakinblocks.auroral.registry;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.inventory.ColdBrewingStandMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for menu types.
 */
public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Auroral.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ColdBrewingStandMenu>> COLD_BREWING_STAND =
        MENU_TYPES.register("cold_brewing_stand",
            () -> new MenuType<>(ColdBrewingStandMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
