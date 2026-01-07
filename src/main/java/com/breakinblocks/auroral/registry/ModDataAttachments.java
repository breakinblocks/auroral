package com.breakinblocks.auroral.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.breakinblocks.auroral.Auroral;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Auroral.MOD_ID);

    /**
     * Aurora state stored on ServerLevel to track if aurora is currently active.
     */
    public record AuroraState(boolean active, long startTick, long endTick) {
        public static final AuroraState INACTIVE = new AuroraState(false, 0, 0);

        public static final MapCodec<AuroraState> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Codec.BOOL.fieldOf("active").forGetter(AuroraState::active),
                Codec.LONG.fieldOf("start_tick").forGetter(AuroraState::startTick),
                Codec.LONG.fieldOf("end_tick").forGetter(AuroraState::endTick)
            ).apply(instance, AuroraState::new)
        );

        public AuroraState withActive(boolean active) {
            return new AuroraState(active, this.startTick, this.endTick);
        }

        public AuroraState start(long gameTime, long duration) {
            return new AuroraState(true, gameTime, gameTime + duration);
        }

        public AuroraState end() {
            return INACTIVE;
        }

        public boolean isExpired(long gameTime) {
            return active && gameTime >= endTick;
        }
    }

    public static final Supplier<AttachmentType<AuroraState>> AURORA_STATE = ATTACHMENT_TYPES.register(
        "aurora_state",
        () -> AttachmentType.builder(() -> AuroraState.INACTIVE)
            .serialize(AuroraState.CODEC)
            .build()
    );
}
