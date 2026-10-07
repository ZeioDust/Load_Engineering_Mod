package com.loadbearing.solver;

import com.loadbearing.Config;
import com.loadbearing.registry.LBAttachments;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;

public final class SnapshotCapture {
    private final ServerLevel level;
    private final BlockPos origin;
    private int operations;

    public SnapshotCapture(ServerLevel level, BlockPos origin) {
        this.level = level;
        this.origin = origin;
    }

    public int operationsUsed() {
        return this.operations;
    }

    public ClusterSnapshot capture(int budget) {
        int radius = Config.SOLVER_RADIUS.get();
        int maxNodes = Config.SOLVER_MAX_NODES.get();
        int allowance = Math.max(64, Math.min(budget, maxNodes));

        int anchorDepth = Config.ANCHOR_DEPTH.get();

        Long2ObjectMap<BlockState> states = new Long2ObjectOpenHashMap<>(Math.min(maxNodes, 4096));
        Long2ObjectMap<BlockState> ground = new Long2ObjectOpenHashMap<>(256);
        LongSet placed = new LongOpenHashSet();
        LongSet reinforced = new LongOpenHashSet();
        LongSet disturbed = new LongOpenHashSet();
        LongSet seen = new LongOpenHashSet(Math.min(maxNodes, 4096));
        LongArrayFIFOQueue frontier = new LongArrayFIFOQueue(256);

        Long2IntMap naturalRun = new Long2IntOpenHashMap(256);
        naturalRun.defaultReturnValue(0);

        seed(frontier, seen, this.origin.asLong());
        for (int dir = 0; dir < 6; dir++) {
            seed(frontier, seen, BlockPos.asLong(
                    this.origin.getX() + DX[dir],
                    this.origin.getY() + DY[dir],
                    this.origin.getZ() + DZ[dir]));
        }

        boolean truncated = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        while (!frontier.isEmpty()) {
            if (states.size() >= maxNodes || this.operations >= allowance) {
                truncated = true;
                break;
            }
            long packed = frontier.dequeueLong();
            int x = BlockPos.getX(packed);
            int y = BlockPos.getY(packed);
            int z = BlockPos.getZ(packed);

            if (Math.abs(x - this.origin.getX()) > radius
                    || Math.abs(y - this.origin.getY()) > radius
                    || Math.abs(z - this.origin.getZ()) > radius) {
                truncated = true;
                continue;
            }
            if (y < this.level.getMinY() || y > this.level.getMaxY()) {
                continue;
            }

            LevelChunk chunk = this.level.getChunkSource().getChunkNow(x >> 4, z >> 4);
            if (chunk == null) {
                truncated = true;
                continue;
            }

            cursor.set(x, y, z);
            BlockState state = chunk.getBlockState(cursor);
            this.operations++;

            if (state.isAir()) {
                continue;
            }

            states.put(packed, state);
            boolean playerPlaced = LBAttachments.placement(chunk).isPlayerPlaced(packed);
            if (playerPlaced) {
                placed.add(packed);
            }
            if (LBAttachments.reinforcement(chunk).isReinforced(packed)) {
                reinforced.add(packed);
            }
            boolean cutInto = LBAttachments.disturbance(chunk).isDisturbed(packed);
            if (cutInto) {
                disturbed.add(packed);
            }

            int run = playerPlaced ? 0 : naturalRun.get(packed) + 1;
            boolean upwardOnly = !playerPlaced && !cutInto;
            if (upwardOnly && run > anchorDepth + 1) {
                continue;
            }

            for (int dir = 0; dir < 6; dir++) {
                if (upwardOnly && DY[dir] <= 0) {
                    continue;
                }
                long next = BlockPos.asLong(x + DX[dir], y + DY[dir], z + DZ[dir]);
                if (seen.add(next)) {
                    naturalRun.put(next, run);
                    frontier.enqueue(next);
                }
            }
        }

        if (states.isEmpty()) {
            return null;
        }
        if (!reinforced.isEmpty()) {
        }

        for (long packed : states.keySet()) {
            long below = BlockPos.asLong(
                    BlockPos.getX(packed), BlockPos.getY(packed) - 1, BlockPos.getZ(packed));
            if (states.containsKey(below) || ground.containsKey(below)) {
                continue;
            }
            int bx = BlockPos.getX(below);
            int by = BlockPos.getY(below);
            int bz = BlockPos.getZ(below);
            if (by < this.level.getMinY()) {
                continue;
            }
            LevelChunk chunk = this.level.getChunkSource().getChunkNow(bx >> 4, bz >> 4);
            if (chunk == null) {
                continue;
            }
            cursor.set(bx, by, bz);
            ground.put(below, chunk.getBlockState(cursor));
            this.operations++;
        }

        return new ClusterSnapshot(
                this.level.dimension().identifier().toString(),
                this.origin.asLong(),
                states,
                placed,
                reinforced,
                disturbed,
                this.level.getMinY(),
                ground,
                truncated);
    }

    private static void seed(LongArrayFIFOQueue frontier, LongSet seen, long packed) {
        if (seen.add(packed)) {
            frontier.enqueue(packed);
        }
    }

    private static final int[] DX = {0, 0, 1, -1, 0, 0};
    private static final int[] DY = {-1, 1, 0, 0, 0, 0};
    private static final int[] DZ = {0, 0, 0, 0, 1, -1};
}
