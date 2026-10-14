package com.loadbearing.net;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class LBClientNetwork {
    private LBClientNetwork() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(LBNetwork.CHANNEL,
                (client, handler, buf, sender) -> {
                    ReinforcementPayload payload = ReinforcementPayload.decode(buf);
                    client.execute(() -> com.loadbearing.client.ClientPayloadHandler
                            .acceptReinforcement(payload));
                });
    }
}
