package com.loadbearing.net;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class LBNetwork {
    private static final String VERSION = "1";

    private LBNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);

        registrar.playToClient(
                ReinforcementPayload.TYPE,
                ReinforcementPayload.STREAM_CODEC,
                LBNetwork::handleReinforcement);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    private static void handleReinforcement(ReinforcementPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.loadbearing.client.ClientPayloadHandler.acceptReinforcement(payload));
    }
}
