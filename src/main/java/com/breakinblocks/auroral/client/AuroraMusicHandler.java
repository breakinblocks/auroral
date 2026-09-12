package com.breakinblocks.auroral.client;

import com.breakinblocks.auroral.Auroral;
import com.breakinblocks.auroral.config.AuroralConfig;
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
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Client-side handler for Aurora music on the music channel.
 * Plays the aurora music track once per aurora event (not looped).
 */
@EventBusSubscriber(modid = Auroral.MOD_ID, value = Dist.CLIENT)
public class AuroraMusicHandler {

    private static AuroraMusicSoundInstance currentMusic = null;
    private static boolean wasPlaying = false;
    private static boolean hasPlayedThisAurora = false;
    private static boolean auroraWasActive = false;

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (AuroralConfig.CLIENT.playAuroraAmbientSound.get()) {
            return;
        }
        var id = event.getOriginalSound().getLocation();
        if (id.equals(ModSounds.AURORA_MUSIC.get().getLocation())
                || id.equals(ModSounds.AURORA_START.get().getLocation())
                || id.equals(ModSounds.AURORA_END.get().getLocation())) {
            event.setSound(null);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }

        if (!AuroralConfig.CLIENT.playAuroraAmbientSound.get()) {
            if (currentMusic != null) {
                forceStop();
            }
            return;
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        boolean auroraActive = ClientAuroraState.isAuroraActive();
        if (auroraWasActive && !auroraActive) {
            // Aurora ended — reset so the next aurora plays the track again.
            hasPlayedThisAurora = false;
            stopMusic();
        }
        auroraWasActive = auroraActive;

        Level level = player.level();
        boolean shouldPlay = auroraActive
            && BiomeHelper.isColdBiome(level, player.blockPosition())
            && level.canSeeSky(player.blockPosition());

        if (shouldPlay && !wasPlaying && !hasPlayedThisAurora) {
            startMusic();
            hasPlayedThisAurora = true;
        }
    }

    private static void startMusic() {
        Minecraft mc = Minecraft.getInstance();
        if (currentMusic == null || !mc.getSoundManager().isActive(currentMusic)) {
            mc.getMusicManager().stopPlaying();

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
        }
        wasPlaying = false;
        hasPlayedThisAurora = false;
        auroraWasActive = false;
    }

    /**
     * Custom sound instance for aurora music with fade-in/fade-out support.
     * Plays once (no looping); max volume is capped at half.
     */
    private static class AuroraMusicSoundInstance extends AbstractTickableSoundInstance {
        private static final int FADE_TICKS = 60;
        private static final float MAX_VOLUME = 0.5f;

        private int fadeCounter = 0;
        private boolean fadingIn = true;
        private boolean fadingOut = false;

        protected AuroraMusicSoundInstance() {
            super(ModSounds.AURORA_MUSIC.get(), SoundSource.MUSIC, SoundInstance.createUnseededRandom());
            this.looping = false;
            this.delay = 0;
            this.volume = 0.0f;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (fadingIn) {
                fadeCounter++;
                this.volume = (fadeCounter / (float) FADE_TICKS) * MAX_VOLUME;
                if (fadeCounter >= FADE_TICKS) {
                    fadingIn = false;
                    this.volume = MAX_VOLUME;
                }
            } else if (fadingOut) {
                fadeCounter--;
                this.volume = Math.max(0.0f, (fadeCounter / (float) FADE_TICKS) * MAX_VOLUME);
                if (fadeCounter <= 0) {
                    this.stop();
                }
            }
        }

        public void fadeOut() {
            if (!fadingOut) {
                fadingOut = true;
                fadingIn = false;
                fadeCounter = (int) ((volume / MAX_VOLUME) * FADE_TICKS);
            }
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }
    }
}
