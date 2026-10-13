package com.loadbearing.net;

import java.util.List;

import com.loadbearing.LoadBearing;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ReinforcementPayload(int chunkX, int chunkZ, boolean replace, List<Long> positions)
        implements CustomPacketPayload {
    public static final int MAX_ENTRIES = 20000;

    public static final CustomPacketPayload.Type<ReinforcementPayload> TYPE =
            new CustomPacketPayload.Type<>(LoadBearing.id("reinforcement"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReinforcementPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ReinforcementPayload::chunkX,
                    ByteBufCodecs.VAR_INT, ReinforcementPayload::chunkZ,
                    ByteBufCodecs.BOOL, ReinforcementPayload::replace,
                    ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list(MAX_ENTRIES)),
                    ReinforcementPayload::positions,
                    ReinforcementPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
