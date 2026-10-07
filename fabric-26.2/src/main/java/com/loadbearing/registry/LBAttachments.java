package com.loadbearing.registry;

import com.loadbearing.LoadBearing;
import com.loadbearing.solver.ChunkDisturbanceData;
import com.loadbearing.solver.ChunkPlacementData;
import com.loadbearing.solver.ChunkReinforcementData;
import com.loadbearing.solver.ChunkSolverData;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Fabric's own data attachments carry the same four per-chunk objects, written with the same
 * codecs under the same ids, so a world keeps its placement, disturbance and reinforcement records
 * exactly as it does on the other targets. The solver cache is deliberately not persisted, as
 * everywhere else.
 */
public final class LBAttachments {
    public static final AttachmentType<ChunkPlacementData> PLACEMENT = AttachmentRegistry.create(
            LoadBearing.id("placement"),
            builder -> builder.initializer(ChunkPlacementData::new)
                    .persistent(ChunkPlacementData.CODEC.codec()));

    public static final AttachmentType<ChunkSolverData> SOLVER_CACHE = AttachmentRegistry.create(
            LoadBearing.id("solver_cache"),
            builder -> builder.initializer(ChunkSolverData::new));

    public static final AttachmentType<ChunkDisturbanceData> DISTURBANCE = AttachmentRegistry.create(
            LoadBearing.id("disturbance"),
            builder -> builder.initializer(ChunkDisturbanceData::new)
                    .persistent(ChunkDisturbanceData.CODEC.codec()));

    public static final AttachmentType<ChunkReinforcementData> REINFORCEMENT =
            AttachmentRegistry.create(LoadBearing.id("reinforcement"),
                    builder -> builder.initializer(ChunkReinforcementData::new)
                            .persistent(ChunkReinforcementData.CODEC.codec()));

    private LBAttachments() {}

    public static void init() {}

    public static ChunkPlacementData placement(ChunkAccess chunk) {
        return chunk.getAttachedOrCreate(PLACEMENT);
    }

    public static ChunkSolverData solverCache(ChunkAccess chunk) {
        return chunk.getAttachedOrCreate(SOLVER_CACHE);
    }

    public static ChunkDisturbanceData disturbance(ChunkAccess chunk) {
        return chunk.getAttachedOrCreate(DISTURBANCE);
    }

    public static ChunkReinforcementData reinforcement(ChunkAccess chunk) {
        return chunk.getAttachedOrCreate(REINFORCEMENT);
    }
}
