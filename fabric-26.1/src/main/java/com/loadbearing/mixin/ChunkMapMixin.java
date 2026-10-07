package com.loadbearing.mixin;

import com.loadbearing.solver.ReinforcementSender;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stands in for the chunk-watch event the other loaders provide: the moment a chunk is queued to
 * be sent to a particular player is the moment that player needs this chunk's reinforcement list.
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @Inject(method = "markChunkPendingToSend(Lnet/minecraft/server/level/ServerPlayer;"
            + "Lnet/minecraft/world/level/chunk/LevelChunk;)V", at = @At("HEAD"))
    private static void loadbearing$sendReinforcement(ServerPlayer player, LevelChunk chunk,
            CallbackInfo callback) {
        ReinforcementSender.sendChunk(player, chunk);
    }
}
