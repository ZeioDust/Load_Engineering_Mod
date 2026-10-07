package com.loadbearing.solver;

import com.loadbearing.Config;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class AnchorRules {
    private AnchorRules() {}

    public static boolean isAnchor(ClusterSnapshot snapshot, long packed, BlockState state) {
        return isGround(snapshot, packed, state);
    }

    public static boolean isGround(ClusterSnapshot snapshot, long packed, BlockState state) {
        if (state.isAir()) {
            return false;
        }
        int y = BlockPos.getY(packed);
        if (y <= snapshot.minY()) {
            return true;
        }
        if (snapshot.disturbed().contains(packed)) {
            return false;
        }
        if (!snapshot.playerPlaced().contains(packed)) {
            return true;
        }
        return burialDepth(snapshot, packed) >= Config.ANCHOR_DEPTH.get();
    }

    private static int burialDepth(ClusterSnapshot snapshot, long packed) {
        int x = BlockPos.getX(packed);
        int y = BlockPos.getY(packed);
        int z = BlockPos.getZ(packed);
        int needed = Config.ANCHOR_DEPTH.get();

        for (int dy = 1; dy <= needed; dy++) {
            long above = BlockPos.asLong(x, y + dy, z);
            BlockState state = snapshot.states().get(above);
            if (state == null || state.isAir() || snapshot.playerPlaced().contains(above)) {
                return dy - 1;
            }
        }
        return needed;
    }
}
