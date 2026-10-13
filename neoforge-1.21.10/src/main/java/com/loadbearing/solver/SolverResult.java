package com.loadbearing.solver;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongList;

public record SolverResult(
        long origin,
        String dimension,
        Long2DoubleMap loads,
        Long2DoubleMap margins,
        LongList unsupported,
        LongList crushed,
        LongList overloadedFoundations,
        int nodesVisited,
        boolean truncated) {
    public LongList failures() {
        it.unimi.dsi.fastutil.longs.LongArrayList all =
                new it.unimi.dsi.fastutil.longs.LongArrayList(this.unsupported.size() + this.crushed.size());
        all.addAll(this.unsupported);
        for (int i = 0; i < this.crushed.size(); i++) {
            long p = this.crushed.getLong(i);
            if (!all.contains(p)) {
                all.add(p);
            }
        }
        return all;
    }
}
