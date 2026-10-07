package com.loadbearing.solver;

import com.loadbearing.material.MaterialProfile;
import com.loadbearing.material.MaterialRegistry;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class StructuralSolver {

    private static final long RANK_STRIDE = 4096L;

    public static final int UNREACHED = Integer.MAX_VALUE;

    private static final int UNLIMITED_REACH = Integer.MAX_VALUE;

    private static final int DISTURBED_SPAN = 1;

    private StructuralSolver() {}

    public static SolverResult solve(ClusterSnapshot snapshot) {
        Long2ObjectMap<BlockState> states = snapshot.states();
        int count = states.size();

        Long2IntMap cost = new Long2IntOpenHashMap(count);
        cost.defaultReturnValue(UNREACHED);
        Long2IntMap reach = new Long2IntOpenHashMap(count);
        reach.defaultReturnValue(-1);

        LongArrayFIFOQueue frontier = new LongArrayFIFOQueue(Math.max(16, Math.min(count, 4096)));

        LongSet ground = new LongOpenHashSet();
        LongSet anchors = new LongOpenHashSet();

        for (Long2ObjectMap.Entry<BlockState> entry : states.long2ObjectEntrySet()) {
            long packed = entry.getLongKey();
            BlockState state = entry.getValue();
            boolean isGround = AnchorRules.isGround(snapshot, packed, state);
            if (isGround) {
                ground.add(packed);
            }
            if (isGround || AnchorRules.isAnchor(snapshot, packed, state)) {
                anchors.add(packed);
                cost.put(packed, 0);

                reach.put(packed, isGround ? UNLIMITED_REACH : MaterialRegistry.maxSpan(state));
                frontier.enqueueFirst(packed);
            }
        }

        LongSet foundations = findFoundations(snapshot, anchors);

        Long2IntMap boost = new Long2IntOpenHashMap(count);
        boost.defaultReturnValue(1);
        for (long packed : anchors) {
            if (isGroutedFoundation(snapshot, foundations, packed)) {
                boost.put(packed, MaterialProfile.REINFORCEMENT_FACTOR);
            }
        }

        int ops = 0;
        while (!frontier.isEmpty()) {
            long from = frontier.dequeueLong();
            int fromCost = cost.get(from);
            int fromReach = reach.get(from);
            int fromBoost = boost.get(from);
            if (fromCost == UNREACHED) {
                continue;
            }
            BlockState fromState = states.get(from);
            if (fromState == null) {
                continue;
            }
            MaterialProfile fromProfile = profileAt(snapshot, foundations, from, fromState);

            int x = BlockPos.getX(from);
            int y = BlockPos.getY(from);
            int z = BlockPos.getZ(from);

            for (int dir = 0; dir < 6; dir++) {
                int nx = x + DX[dir];
                int ny = y + DY[dir];
                int nz = z + DZ[dir];
                boolean horizontal = DY[dir] == 0;
                boolean downward = DY[dir] < 0;

                if (fromProfile.tensionOnly() && !downward) {
                    continue;
                }

                long to = BlockPos.asLong(nx, ny, nz);
                BlockState toState = states.get(to);
                if (toState == null || toState.isAir()) {
                    continue;
                }

                MaterialProfile toProfile = profileAt(snapshot, foundations, to, toState);

                if (toProfile.tensionOnly() && !downward) {
                    continue;
                }

                if (downward && snapshot.disturbed().contains(to)) {
                    continue;
                }

                int stepCost = horizontal ? 1 : 0;
                int newCost = fromCost + stepCost;

                int newReach = horizontal
                        ? Math.min(fromReach, spanOf(snapshot, foundations, to, toProfile))
                        : fromReach;

                int newBoost = Math.max(fromBoost,
                        isGroutedFoundation(snapshot, foundations, to)
                                ? MaterialProfile.REINFORCEMENT_FACTOR : 1);

                if (newCost > (long) newReach * newBoost) {
                    continue;
                }

                int known = cost.get(to);
                int knownReach = reach.get(to);
                int knownBoost = boost.get(to);

                boolean better = newCost < known || newReach > knownReach || newBoost > knownBoost;
                if (!better) {
                    continue;
                }

                cost.put(to, newCost);
                reach.put(to, newReach);
                boost.put(to, newBoost);
                if (stepCost == 0) {
                    frontier.enqueueFirst(to);
                } else {
                    frontier.enqueue(to);
                }
                ops++;
            }
            ops++;
        }

        LongArrayList ordered = new LongArrayList(count);
        for (long packed : states.keySet()) {
            if (cost.get(packed) != UNREACHED) {
                ordered.add(packed);
            }
        }
        final Long2IntMap costView = cost;
        final int floor = snapshot.minY();
        long[] orderedArray = ordered.toLongArray();
        LongComparator byDescendingRank = new LongComparator() {
            @Override
            public int compare(long a, long b) {
                return Long.compare(rank(b, costView, floor), rank(a, costView, floor));
            }
        };
        LongArrays.quickSort(orderedArray, byDescendingRank);

        Long2DoubleMap carried = new Long2DoubleOpenHashMap(orderedArray.length);
        carried.defaultReturnValue(0.0D);
        for (long packed : orderedArray) {
            carried.put(packed, MaterialRegistry.weight(states.get(packed)));
        }

        LongArrayList supporters = new LongArrayList(4);
        for (long packed : orderedArray) {
            double load = carried.get(packed);
            if (load <= 0.0D) {
                continue;
            }
            if (anchors.contains(packed)) {
                continue;
            }
            supporters.clear();
            collectSupporters(snapshot, cost, packed, supporters);
            if (supporters.isEmpty()) {
                continue;
            }
            double share = load / supporters.size();
            for (int s = 0; s < supporters.size(); s++) {
                long sup = supporters.getLong(s);
                carried.put(sup, carried.get(sup) + share);
            }
            ops += supporters.size();
        }

        Long2DoubleMap margins = new Long2DoubleOpenHashMap(count);
        LongList unsupported = new LongArrayList();
        LongList crushed = new LongArrayList();
        LongList overloadedFoundations = new LongArrayList();

        for (Long2ObjectMap.Entry<BlockState> entry : states.long2ObjectEntrySet()) {
            long packed = entry.getLongKey();
            BlockState state = entry.getValue();
            MaterialProfile profile = profileAt(snapshot, foundations, packed, state);

            if (cost.get(packed) == UNREACHED) {
                if (snapshot.truncated()) {
                    margins.put(packed, 0.0D);
                    continue;
                }
                unsupported.add(packed);
                margins.put(packed, -100.0D);
                continue;
            }

            double load = carried.get(packed);
            double margin = profile.safetyMargin(load);
            margins.put(packed, margin);

            if (!ground.contains(packed)
                    && profile.carriesCompression()
                    && load > profile.scaledStrength()) {
                crushed.add(packed);
            }
        }

        return new SolverResult(
                snapshot.origin(),
                snapshot.dimension(),
                carried,
                margins,
                unsupported,
                crushed,
                overloadedFoundations,
                ops,
                snapshot.truncated());
    }

    private static void collectSupporters(
            ClusterSnapshot snapshot, Long2IntMap cost, long packed, LongArrayList out) {
        int x = BlockPos.getX(packed);
        int y = BlockPos.getY(packed);
        int z = BlockPos.getZ(packed);
        int myCost = cost.get(packed);

        long below = BlockPos.asLong(x, y - 1, z);
        BlockState belowState = snapshot.states().get(below);
        if (belowState != null && !belowState.isAir()
                && MaterialRegistry.get(belowState).carriesCompression()
                && cost.get(below) != UNREACHED) {
            out.add(below);
            return;
        }

        for (int dir = 0; dir < 4; dir++) {
            long side = BlockPos.asLong(x + HX[dir], y, z + HZ[dir]);
            BlockState sideState = snapshot.states().get(side);
            if (sideState == null || sideState.isAir()) {
                continue;
            }
            MaterialProfile sideProfile = MaterialRegistry.get(sideState);
            if (!sideProfile.carriesCompression()) {
                continue;
            }
            int sideCost = cost.get(side);
            if (sideCost == UNREACHED || sideCost >= myCost) {
                continue;
            }
            out.add(side);
        }
    }

    private static long rank(long packed, Long2IntMap cost, int minY) {
        int c = cost.get(packed);
        int y = BlockPos.getY(packed) - minY;
        return (long) c * RANK_STRIDE + y;
    }

    private static LongSet findFoundations(ClusterSnapshot snapshot, LongSet anchors) {
        Long2ObjectMap<BlockState> states = snapshot.states();
        LongSet found = new LongOpenHashSet(Math.min(states.size(), 1024));
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue(Math.max(16, Math.min(anchors.size(), 256)));
        for (long packed : anchors) {
            if (found.add(packed)) {
                queue.enqueue(packed);
            }
        }
        while (!queue.isEmpty()) {
            long packed = queue.dequeueLong();
            int x = BlockPos.getX(packed);
            int y = BlockPos.getY(packed);
            int z = BlockPos.getZ(packed);
            for (int dy = -1; dy <= 1; dy += 2) {
                long next = BlockPos.asLong(x, y + dy, z);
                BlockState state = states.get(next);
                if (state == null || state.isAir() || !found.add(next)) {
                    continue;
                }
                queue.enqueue(next);
            }
        }
        return found;
    }

    private static int spanOf(ClusterSnapshot snapshot, LongSet foundations, long packed,
            MaterialProfile profile) {
        if (!snapshot.disturbed().contains(packed)) {
            return profile.scaledSpan();
        }
        int span = DISTURBED_SPAN;
        return isGroutedFoundation(snapshot, foundations, packed)
                ? span * MaterialProfile.REINFORCEMENT_FACTOR : span;
    }

    private static boolean isGroutedFoundation(ClusterSnapshot snapshot, LongSet foundations, long packed) {
        return snapshot.reinforced().contains(packed) && foundations.contains(packed);
    }

    private static MaterialProfile profileAt(ClusterSnapshot snapshot, LongSet foundations,
            long packed, BlockState state) {
        MaterialProfile profile = MaterialRegistry.get(state);
        return isGroutedFoundation(snapshot, foundations, packed) ? profile.reinforced() : profile;
    }

    private static final int[] DX = {0, 0, 1, -1, 0, 0};
    private static final int[] DY = {-1, 1, 0, 0, 0, 0};
    private static final int[] DZ = {0, 0, 0, 0, 1, -1};

    private static final int[] HX = {1, -1, 0, 0};
    private static final int[] HZ = {0, 0, 1, -1};
}
