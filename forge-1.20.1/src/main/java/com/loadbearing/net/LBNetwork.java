package com.loadbearing.net;

import com.loadbearing.LoadBearing;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * This era had no payload registry, so the one clientbound packet goes over a SimpleChannel. The
 * protocol version string is the same "1" the payload registrar declared.
 */
public final class LBNetwork {
    private static final String VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            LoadBearing.id("reinforcement"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals);

    private LBNetwork() {}

    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CHANNEL.messageBuilder(ReinforcementPayload.class, 0)
                .encoder(ReinforcementPayload::encode)
                .decoder(ReinforcementPayload::decode)
                .consumerMainThread(LBNetwork::handleReinforcement)
                .add());
    }

    public static void sendToPlayer(ServerPlayer player, ReinforcementPayload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    private static void handleReinforcement(ReinforcementPayload payload,
            java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                com.loadbearing.client.ClientPayloadHandler.acceptReinforcement(payload);
            }
        });
        context.get().setPacketHandled(true);
    }
}
