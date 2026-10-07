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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;

public final class ReinforcementSender {
    private ReinforcementSender() {}

    public static void register(IEventBus bus) {
        bus.addListener(ReinforcementSender::onChunkWatch);
    }

    private static void onChunkWatch(ChunkWatchEvent.Sent event) {
        LevelChunk chunk = event.getChunk();
        ChunkReinforcementData data = chunk.getData(LBAttachments.REINFORCEMENT);
        if (data.isEmpty()) {
            return;
        }
        ChunkPos pos = event.getPos();
        LBNetwork.sendToPlayer(event.getPlayer(),
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
