package com.loadbearing.solver;

import java.util.List;

import com.loadbearing.net.LBNetwork;
import com.loadbearing.net.ReinforcementPayload;
import com.loadbearing.registry.LBAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

public final class ReinforcementSender {
    private ReinforcementSender() {}

    // This loader has no chunk-watch event; the mixin on the chunk map calls sendChunk directly.
    public static void register() {}

    public static void sendChunk(ServerPlayer player, LevelChunk chunk) {
        ChunkReinforcementData data = LBAttachments.reinforcement(chunk);
        if (data.isEmpty()) {
            return;
        }
        ChunkPos pos = chunk.getPos();
        LBNetwork.sendToPlayer(player,
                new ReinforcementPayload(pos.x(), pos.z(), true, data.toList()));
    }

    public static void broadcastOne(ServerLevel level, BlockPos pos) {
        ChunkPos chunk = ChunkPos.containing(pos);
        ReinforcementPayload payload =
                new ReinforcementPayload(chunk.x(), chunk.z(), false, List.of(pos.asLong()));
        for (ServerPlayer player : level.players()) {
            if (player.chunkPosition().getChessboardDistance(chunk) <= player.requestedViewDistance()) {
                LBNetwork.sendToPlayer(player, payload);
            }
        }
    }
}
