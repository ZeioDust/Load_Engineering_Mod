package com.loadbearing.solver;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.block.state.BlockState;

public record ClusterSnapshot(
        String dimension,
        long origin,
        Long2ObjectMap<BlockState> states,
        LongSet playerPlaced,
        LongSet reinforced,
        LongSet disturbed,
        int minY,
        Long2ObjectMap<BlockState> groundBelow,
        boolean truncated) {
    public int size() {
        return this.states.size();
    }

    public boolean isEmpty() {
        return this.states.isEmpty();
    }
}
