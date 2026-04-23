package com.breakinblocks.auroral.events;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.block.ShimmeringIceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

/**
 * Re-registers farmland-hydration water tickets for Shimmering Ice blocks when chunks load.
 * Without this, ice placed before the world was last saved (or placed by worldgen/structure)
 * would not hydrate vanilla farmland until the player broke and re-placed each block.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID)
public class ShimmeringIceChunkHandler {

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ChunkAccess chunk = event.getChunk();
        // The chunk isn't promoted to FULL when this event fires; defer the scan to the next tick
        // so Level interactions are safe.
        MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }
        server.tell(new TickTask(server.getTickCount() + 1, () -> scanAndRegister(level, chunk)));
    }

    private static void scanAndRegister(ServerLevel level, ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int baseX = chunkPos.getMinBlockX();
        int baseZ = chunkPos.getMinBlockZ();
        LevelChunkSection[] sections = chunk.getSections();
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section == null || section.hasOnlyAir()) {
                continue;
            }
            if (!section.maybeHas(s -> s.getBlock() instanceof ShimmeringIceBlock)) {
                continue;
            }
            int baseY = chunk.getSectionYFromSectionIndex(sectionIndex) << 4;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (!(section.getBlockState(x, y, z).getBlock() instanceof ShimmeringIceBlock)) {
                            continue;
                        }
                        scratch.set(baseX + x, baseY + y, baseZ + z);
                        ShimmeringIceBlock.registerWaterTicket(level, scratch);
                    }
                }
            }
        }
    }
}
