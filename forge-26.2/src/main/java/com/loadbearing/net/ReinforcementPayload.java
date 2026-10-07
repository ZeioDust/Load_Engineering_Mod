package com.loadbearing.net;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;

/**
 * This loader has no CustomPacketPayload registry, so the record carries its own reader and writer.
 * The wire shape is unchanged: chunk x, chunk z, the replace flag, then a length-prefixed run of
 * var-longs capped at the same MAX_ENTRIES.
 */
public record ReinforcementPayload(int chunkX, int chunkZ, boolean replace, List<Long> positions) {
    public static final int MAX_ENTRIES = 20000;

    public static void encode(ReinforcementPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.chunkX());
        buf.writeVarInt(payload.chunkZ());
        buf.writeBoolean(payload.replace());
        List<Long> positions = payload.positions();
        buf.writeVarInt(positions.size());
        for (long packed : positions) {
            buf.writeVarLong(packed);
        }
    }

    public static ReinforcementPayload decode(FriendlyByteBuf buf) {
        int chunkX = buf.readVarInt();
        int chunkZ = buf.readVarInt();
        boolean replace = buf.readBoolean();
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_ENTRIES) {
            throw new IllegalArgumentException("Reinforcement payload too long: " + count);
        }
        List<Long> positions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            positions.add(buf.readVarLong());
        }
        return new ReinforcementPayload(chunkX, chunkZ, replace, List.copyOf(positions));
    }
}
