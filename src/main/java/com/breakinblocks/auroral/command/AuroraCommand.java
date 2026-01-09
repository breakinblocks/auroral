package com.breakinblocks.auroral.command;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.net.AuroralNetworking;
import com.breakinblocks.auroral.registry.ModDataAttachments.AuroraState;
import com.breakinblocks.auroral.util.AuroraHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Development commands for testing aurora and weather events.
 * All commands require operator permissions (level 2).
 */
@EventBusSubscriber(modid = Auroral.MOD_ID)
public class AuroraCommand {

    private static final int DEFAULT_DURATION = 6000; // 5 minutes in ticks

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("aurora")
            .requires(source -> source.hasPermission(2)) // Op level 2
            .then(Commands.literal("start")
                .executes(ctx -> startAurora(ctx, DEFAULT_DURATION))
                .then(Commands.argument("duration", IntegerArgumentType.integer(100, 240000))
                    .executes(ctx -> startAurora(ctx, IntegerArgumentType.getInteger(ctx, "duration")))
                )
            )
            .then(Commands.literal("stop")
                .executes(AuroraCommand::stopAurora)
            )
            .then(Commands.literal("status")
                .executes(AuroraCommand::getStatus)
            )
        );

        Auroral.LOGGER.debug("Registered /aurora command");
    }

    /**
     * Starts an aurora event with the specified duration.
     */
    private static int startAurora(CommandContext<CommandSourceStack> ctx, int duration) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();

        // Start the aurora
        AuroraHelper.startAurora(level, duration);

        // Sync to all clients
        AuroralNetworking.syncAuroraToAll(level, true);

        // Calculate duration in seconds for display
        int seconds = duration / 20;
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        String timeStr;
        if (minutes > 0) {
            timeStr = String.format("%d min %d sec", minutes, remainingSeconds);
        } else {
            timeStr = String.format("%d sec", seconds);
        }

        source.sendSuccess(() -> Component.literal("Aurora started for " + timeStr + " (" + duration + " ticks)"), true);
        return 1;
    }

    /**
     * Stops the current aurora event.
     */
    private static int stopAurora(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();

        boolean wasActive = AuroraHelper.isAuroraActive(level);

        // End the aurora
        AuroraHelper.endAurora(level);

        // Sync to all clients
        AuroralNetworking.syncAuroraToAll(level, false);

        if (wasActive) {
            source.sendSuccess(() -> Component.literal("Aurora stopped"), true);
        } else {
            source.sendSuccess(() -> Component.literal("Aurora was not active"), false);
        }
        return 1;
    }

    /**
     * Shows the current aurora status.
     */
    private static int getStatus(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();

        AuroraState state = AuroraHelper.getAuroraState(level);
        long gameTime = level.getGameTime();
        long dayTime = level.getDayTime() % 24000;

        boolean isNight = AuroraHelper.isNightTime(level);

        source.sendSuccess(() -> Component.literal("=== Aurora Status ==="), false);
        source.sendSuccess(() -> Component.literal("Active: " + (state.active() ? "Yes" : "No")), false);

        if (state.active()) {
            long remaining = state.endTick() - gameTime;
            int remainingSec = (int) (remaining / 20);
            source.sendSuccess(() -> Component.literal("Time remaining: " + remainingSec + " sec (" + remaining + " ticks)"), false);
        }

        source.sendSuccess(() -> Component.literal("Day time: " + dayTime + "/24000 (" + (isNight ? "Night" : "Day") + ")"), false);
        source.sendSuccess(() -> Component.literal("Game time: " + gameTime), false);

        return 1;
    }
}
