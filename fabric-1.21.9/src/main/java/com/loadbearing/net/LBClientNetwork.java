package com.loadbearing.net;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class LBClientNetwork {
    private LBClientNetwork() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ReinforcementPayload.TYPE,
                (payload, context) -> context.client().execute(
                        () -> com.loadbearing.client.ClientPayloadHandler.acceptReinforcement(payload)));
    }
}
