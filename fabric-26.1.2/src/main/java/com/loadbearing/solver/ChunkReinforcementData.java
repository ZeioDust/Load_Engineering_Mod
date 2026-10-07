package com.loadbearing.solver;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;

public final class ChunkReinforcementData {
    public static final MapCodec<ChunkReinforcementData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.LONG.listOf().optionalFieldOf("reinforced", List.of())
                    .forGetter(ChunkReinforcementData::toList)
    ).apply(i, ChunkReinforcementData::new));

    private final LongSet reinforced;

    public ChunkReinforcementData() {
        this.reinforced = new LongOpenHashSet();
    }

    public ChunkReinforcementData(List<Long> from) {
        this.reinforced = new LongOpenHashSet(from.size());
        for (long l : from) {
            this.reinforced.add(l);
        }
    }

    public boolean isReinforced(BlockPos pos) {
        return this.reinforced.contains(pos.asLong());
    }

    public boolean isReinforced(long packed) {
        return this.reinforced.contains(packed);
    }

    public boolean reinforce(BlockPos pos) {
        return this.reinforced.add(pos.asLong());
    }

    public void clear(BlockPos pos) {
        this.reinforced.remove(pos.asLong());
    }

    public boolean isEmpty() {
        return this.reinforced.isEmpty();
    }

    public int size() {
        return this.reinforced.size();
    }

    public LongSet positions() {
        return this.reinforced;
    }

    public List<Long> toList() {
        List<Long> out = new ArrayList<>(this.reinforced.size());
        this.reinforced.forEach((long l) -> out.add(l));
        return out;
    }
}
