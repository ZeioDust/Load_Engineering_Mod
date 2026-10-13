package com.loadbearing.solver;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;

public final class ChunkPlacementData {
    public static final MapCodec<ChunkPlacementData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.LONG.listOf().optionalFieldOf("placed", List.of()).forGetter(ChunkPlacementData::toList)
    ).apply(i, ChunkPlacementData::new));

    private final LongSet placed;

    public ChunkPlacementData() {
        this.placed = new LongOpenHashSet();
    }

    public ChunkPlacementData(List<Long> from) {
        this.placed = new LongOpenHashSet(from.size());
        for (long l : from) {
            this.placed.add(l);
        }
    }

    public boolean isPlayerPlaced(BlockPos pos) {
        return this.placed.contains(pos.asLong());
    }

    public boolean isPlayerPlaced(long packed) {
        return this.placed.contains(packed);
    }

    public void markPlaced(BlockPos pos) {
        this.placed.add(pos.asLong());
    }

    public void clearPlaced(BlockPos pos) {
        this.placed.remove(pos.asLong());
    }

    public boolean isEmpty() {
        return this.placed.isEmpty();
    }

    public int size() {
        return this.placed.size();
    }

    public List<Long> toList() {
        List<Long> out = new ArrayList<>(this.placed.size());
        this.placed.forEach((long l) -> out.add(l));
        return out;
    }
}
