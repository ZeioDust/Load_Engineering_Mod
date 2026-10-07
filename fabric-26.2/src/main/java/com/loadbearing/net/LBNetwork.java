package com.loadbearing.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/**
 * The payload itself is vanilla API and unchanged; only the registration and the send differ.
 */
public final class LBNetwork {
    private LBNetwork() {}

    public static void register() {
        PayloadTypeRegistry.clientboundPlay()
                .register(ReinforcementPayload.TYPE, ReinforcementPayload.STREAM_CODEC);
    }

    public static void sendToPlayer(ServerPlayer player, ReinforcementPayload payload) {
        if (ServerPlayNetworking.canSend(player, ReinforcementPayload.TYPE)) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
