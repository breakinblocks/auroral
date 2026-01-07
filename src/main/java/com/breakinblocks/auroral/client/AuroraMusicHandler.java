package com.breakinblocks.auroral.client;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.registry.ModSounds;
import com.breakinblocks.auroral.util.BiomeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Client-side handler for Aurora music on the weather channel.
 * Plays ambient aurora music when the aurora is active and the player is in a cold biome.
 */
@EventBusSubscriber(modid = Auroral.MOD_ID, value = Dist.CLIENT)
public class AuroraMusicHandler {

    private static AuroraMusicSoundInstance currentMusic = null;
    private static boolean wasPlaying = false;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }

        // Only check every 20 ticks (1 second) to reduce overhead
        if (player.tickCount % 20 != 0) {
            return;
        }

        Level level = player.level();
        boolean shouldPlay = ClientAuroraState.isAuroraActive()
            && BiomeHelper.isColdBiome(level, player.blockPosition())
            && level.canSeeSky(player.blockPosition());

        if (shouldPlay && !wasPlaying) {
            // Start aurora music
            startMusic();
        } else if (!shouldPlay && wasPlaying) {
            // Stop aurora music
            stopMusic();
        }
    }

    private static void startMusic() {
        Minecraft mc = Minecraft.getInstance();
        if (currentMusic == null || !mc.getSoundManager().isActive(currentMusic)) {
            currentMusic = new AuroraMusicSoundInstance();
            mc.getSoundManager().play(currentMusic);
            wasPlaying = true;
            Auroral.LOGGER.debug("Started aurora music");
        }
    }

    private static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.fadeOut();
            wasPlaying = false;
            Auroral.LOGGER.debug("Stopping aurora music (fade out)");
        }
    }

    /**
     * Force stop all aurora music. Called on disconnect.
     */
    public static void forceStop() {
        if (currentMusic != null) {
            Minecraft.getInstance().getSoundManager().stop(currentMusic);
            currentMusic = null;
            wasPlaying = false;
        }
    }

    /**
     * Custom sound instance for aurora music with fade-in/fade-out support.
     */
    private static class AuroraMusicSoundInstance extends AbstractTickableSoundInstance {
        private static final int FADE_TICKS = 60; // 3 seconds
        private int fadeCounter = 0;
        private boolean fadingIn = true;
        private boolean fadingOut = false;

        protected AuroraMusicSoundInstance() {
            super(ModSounds.AURORA_MUSIC.get(), SoundSource.WEATHER, SoundInstance.createUnseededRandom());
            this.looping = true;
            this.delay = 0;
            this.volume = 0.0f; // Start silent for fade-in
            this.relative = true; // Plays relative to player
        }

        @Override
        public void tick() {
            if (fadingIn) {
                fadeCounter++;
                this.volume = Math.min(1.0f, fadeCounter / (float) FADE_TICKS);
                if (fadeCounter >= FADE_TICKS) {
                    fadingIn = false;
                    this.volume = 1.0f;
                }
            } else if (fadingOut) {
                fadeCounter--;
                this.volume = Math.max(0.0f, fadeCounter / (float) FADE_TICKS);
                if (fadeCounter <= 0) {
                    this.stop();
                }
            }
        }

        public void fadeOut() {
            if (!fadingOut) {
                fadingOut = true;
                fadingIn = false;
                // Start fade from current position
                fadeCounter = (int) (volume * FADE_TICKS);
            }
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }
    }
}
