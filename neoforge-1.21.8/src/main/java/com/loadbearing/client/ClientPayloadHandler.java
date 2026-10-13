package com.loadbearing.client;

import com.loadbearing.net.ReinforcementPayload;

public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void acceptReinforcement(ReinforcementPayload payload) {
        ReinforcementOverlay.accept(payload);
    }
}
