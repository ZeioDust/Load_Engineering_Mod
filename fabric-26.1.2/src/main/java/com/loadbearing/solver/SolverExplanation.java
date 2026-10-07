package com.loadbearing.solver;

import com.loadbearing.Config;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public record SolverExplanation(
        int clusterSize,
        int anchorCount,
        boolean truncated,
        boolean inCluster,
        boolean playerPlaced,
        boolean ground,
        boolean anchor,
        boolean reached,
        boolean reinforced,
        double load,
        double margin) {
    public static SolverExplanation of(ServerLevel level, BlockPos pos) {
        SnapshotCapture capture = new SnapshotCapture(level, pos);
        ClusterSnapshot snapshot = capture.capture(Config.SOLVER_OPS_PER_TICK.get());
        if (snapshot == null) {
            return new SolverExplanation(0, 0, false, false, false, false, false, false, false, 0, 0);
        }

        long packed = pos.asLong();
        BlockState state = snapshot.states().get(packed);

        int anchorCount = 0;
        for (Long2ObjectMap.Entry<BlockState> entry : snapshot.states().long2ObjectEntrySet()) {
            if (AnchorRules.isAnchor(snapshot, entry.getLongKey(), entry.getValue())) {
                anchorCount++;
            }
        }

        SolverResult result = StructuralSolver.solve(snapshot);
        boolean reached = !result.unsupported().contains(packed);
        double margin = result.margins().containsKey(packed) ? result.margins().get(packed) : Double.NaN;
        double load = result.loads().containsKey(packed) ? result.loads().get(packed) : Double.NaN;

        return new SolverExplanation(
                snapshot.size(),
                anchorCount,
                snapshot.truncated(),
                state != null,
                snapshot.playerPlaced().contains(packed),
                state != null && AnchorRules.isGround(snapshot, packed, state),
                state != null && AnchorRules.isAnchor(snapshot, packed, state),
                reached,
                snapshot.reinforced().contains(packed),
                load,
                margin);
    }

    public static List<BlockPos> sampleAnchors(ServerLevel level, BlockPos pos, int limit) {
        SnapshotCapture capture = new SnapshotCapture(level, pos);
        ClusterSnapshot snapshot = capture.capture(Config.SOLVER_OPS_PER_TICK.get());
        List<BlockPos> out = new ArrayList<>();
        if (snapshot == null) {
            return out;
        }
        for (Long2ObjectMap.Entry<BlockState> entry : snapshot.states().long2ObjectEntrySet()) {
            if (out.size() >= limit) {
                break;
            }
            if (AnchorRules.isAnchor(snapshot, entry.getLongKey(), entry.getValue())) {
                out.add(BlockPos.of(entry.getLongKey()));
            }
        }
        return out;
    }

    public List<String> lines() {
        List<String> out = new ArrayList<>();
        out.add("cluster " + this.clusterSize + (this.truncated ? " (truncated)" : "")
                + ", anchors " + this.anchorCount);
        out.add("in cluster " + this.inCluster + ", player placed " + this.playerPlaced);
        out.add("ground " + this.ground + ", anchor " + this.anchor + ", reached " + this.reached);
        out.add("load " + String.format("%.2f", this.load)
                + ", margin " + String.format("%.1f", this.margin) + "%");
        if (this.reinforced) {
            out.add("reinforced: span and strength doubled");
        }
        return out;
    }
}
