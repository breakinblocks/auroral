package dev.saereth.aurorabound;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Aurorabound.MOD_ID)
public class Aurorabound {
    public static final String MOD_ID = "aurorabound";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public Aurorabound(IEventBus eventBus, ModContainer container, Dist dist) {
        LOGGER.info("Aurorabound initializing...");

        if (dist.isClient()) {
            // Client-specific setup
        }
    }
}
