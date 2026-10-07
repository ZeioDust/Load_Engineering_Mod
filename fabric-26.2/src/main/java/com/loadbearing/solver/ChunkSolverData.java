package com.loadbearing.solver;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

public final class ChunkSolverData {
    public static final double UNKNOWN = Double.NaN;

    private final Long2DoubleOpenHashMap margins = new Long2DoubleOpenHashMap();
    private final Long2DoubleOpenHashMap loads = new Long2DoubleOpenHashMap();
    private volatile long stamp;

    public ChunkSolverData() {
        this.margins.defaultReturnValue(UNKNOWN);
        this.loads.defaultReturnValue(UNKNOWN);
    }

    public synchronized void record(long packedPos, double load, double margin) {
        this.loads.put(packedPos, load);
        this.margins.put(packedPos, margin);
    }

    public synchronized double margin(long packedPos) {
        return this.margins.get(packedPos);
    }

    public synchronized double load(long packedPos) {
        return this.loads.get(packedPos);
    }

    public synchronized void forget(long packedPos) {
        this.margins.remove(packedPos);
        this.loads.remove(packedPos);
    }

    public synchronized void invalidate() {
        this.margins.clear();
        this.loads.clear();
        this.stamp++;
    }

    public synchronized int size() {
        return this.margins.size();
    }

    public synchronized void bump() {
        this.stamp++;
    }
}
