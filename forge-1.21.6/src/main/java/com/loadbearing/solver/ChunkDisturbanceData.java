package com.loadbearing.solver;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;

public final class ChunkDisturbanceData {
    public static final MapCodec<ChunkDisturbanceData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.LONG.listOf().optionalFieldOf("disturbed", List.of())
                    .forGetter(ChunkDisturbanceData::toList)
    ).apply(i, ChunkDisturbanceData::new));

    private final LongSet disturbed;

    public ChunkDisturbanceData() {
        this.disturbed = new LongOpenHashSet();
    }

    public ChunkDisturbanceData(List<Long> from) {
        this.disturbed = new LongOpenHashSet(from.size());
        for (long l : from) {
            this.disturbed.add(l);
        }
    }

    public boolean isDisturbed(BlockPos pos) {
        return this.disturbed.contains(pos.asLong());
    }

    public boolean isDisturbed(long packed) {
        return this.disturbed.contains(packed);
    }

    public boolean disturb(BlockPos pos) {
        return this.disturbed.add(pos.asLong());
    }

    public void clear(BlockPos pos) {
        this.disturbed.remove(pos.asLong());
    }

    public boolean isEmpty() {
        return this.disturbed.isEmpty();
    }

    public int size() {
        return this.disturbed.size();
    }

    public List<Long> toList() {
        List<Long> out = new ArrayList<>(this.disturbed.size());
        this.disturbed.forEach((long l) -> out.add(l));
        return out;
    }
}
