package com.loadbearing.net;

import com.loadbearing.LoadBearing;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * This era identifies packets by a channel id rather than a registered payload type.
 */
public final class LBNetwork {
    public static final ResourceLocation CHANNEL = LoadBearing.id("reinforcement");

    private LBNetwork() {}

    public static void register() {}

    public static void sendToPlayer(ServerPlayer player, ReinforcementPayload payload) {
        if (ServerPlayNetworking.canSend(player, CHANNEL)) {
            ServerPlayNetworking.send(player, CHANNEL, payload.encode());
        }
    }
}
