package com.breakinblocks.auroral.mixin;

import com.breakinblocks.auroral.events.PlayerEventHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Captures successful teleports, including mods using the normal player teleport API. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {
    @Shadow public ServerPlayer player;
    @Unique private ServerLevel auroral$teleportLevel;
    @Unique private Vec3 auroral$teleportOrigin;

    @Inject(method = "teleport(DDDFFLjava/util/Set;)V", at = @At("HEAD"))
    private void auroral$captureOrigin(CallbackInfo ci) {
        auroral$teleportLevel = player.serverLevel();
        auroral$teleportOrigin = player.position();
    }

    @Inject(method = "teleport(DDDFFLjava/util/Set;)V", at = @At("RETURN"))
    private void auroral$bringFollowers(CallbackInfo ci) {
        ServerLevel sourceLevel = auroral$teleportLevel;
        Vec3 origin = auroral$teleportOrigin;
        auroral$teleportLevel = null;
        auroral$teleportOrigin = null;
        PlayerEventHandler.onSameDimensionTeleport(player, sourceLevel, origin);
    }
}
