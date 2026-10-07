package com.loadbearing.net;

import com.loadbearing.LoadBearing;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

/**
 * This loader has no payload registry, so the one clientbound packet goes over a SimpleChannel.
 * The protocol version is the same "1" the payload registrar declared.
 */
public final class LBNetwork {
    private static final int VERSION = 1;

    private static final SimpleChannel CHANNEL = ChannelBuilder
            .named(LoadBearing.id("reinforcement"))
            .networkProtocolVersion(VERSION)
            .acceptedVersions((status, version) -> version == VERSION)
            .simpleChannel();

    private LBNetwork() {}

    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CHANNEL.messageBuilder(ReinforcementPayload.class, 0)
                .encoder(ReinforcementPayload::encode)
                .decoder(ReinforcementPayload::decode)
                .consumerMainThread(LBNetwork::handleReinforcement)
                .add());
    }

    public static void sendToPlayer(ServerPlayer player, ReinforcementPayload payload) {
        CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
    }

    private static void handleReinforcement(ReinforcementPayload payload,
            CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                com.loadbearing.client.ClientPayloadHandler.acceptReinforcement(payload);
            }
        });
        context.setPacketHandled(true);
    }
}
