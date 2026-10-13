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
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ReinforcementSender {
    private ReinforcementSender() {}

    public static void register(IEventBus bus) {
        bus.addListener(ReinforcementSender::onChunkWatch);
    }

    // This era called the "a player started watching this chunk" event Watch rather than Sent.
    private static void onChunkWatch(ChunkWatchEvent.Watch event) {
        LevelChunk chunk = event.getChunk();
        ChunkReinforcementData data = LBAttachments.reinforcement(chunk);
        if (data.isEmpty()) {
            return;
        }
        ChunkPos pos = event.getPos();
        LBNetwork.sendToPlayer(event.getPlayer(),
                new ReinforcementPayload(pos.x, pos.z, true, data.toList()));
    }

    public static void broadcastOne(ServerLevel level, BlockPos pos) {
        ChunkPos chunk = new ChunkPos(pos);
        ReinforcementPayload payload =
                new ReinforcementPayload(chunk.x, chunk.z, false, List.of(pos.asLong()));
        for (ServerPlayer player : level.players()) {
            if (player.chunkPosition().getChessboardDistance(chunk)
                    <= level.getServer().getPlayerList().getViewDistance()) {
                LBNetwork.sendToPlayer(player, payload);
            }
        }
    }
}
