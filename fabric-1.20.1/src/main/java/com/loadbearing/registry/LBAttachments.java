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
 * Same four per-chunk objects, same ids, same codecs. This era's attachment registry is reached
 * through a builder rather than a create-with-configurator call.
 */
public final class LBAttachments {
    public static final AttachmentType<ChunkPlacementData> PLACEMENT =
            AttachmentRegistry.<ChunkPlacementData>builder()
                    .initializer(ChunkPlacementData::new)
                    .persistent(ChunkPlacementData.CODEC.codec())
                    .buildAndRegister(LoadBearing.id("placement"));

    public static final AttachmentType<ChunkSolverData> SOLVER_CACHE =
            AttachmentRegistry.<ChunkSolverData>builder()
                    .initializer(ChunkSolverData::new)
                    .buildAndRegister(LoadBearing.id("solver_cache"));

    public static final AttachmentType<ChunkDisturbanceData> DISTURBANCE =
            AttachmentRegistry.<ChunkDisturbanceData>builder()
                    .initializer(ChunkDisturbanceData::new)
                    .persistent(ChunkDisturbanceData.CODEC.codec())
                    .buildAndRegister(LoadBearing.id("disturbance"));

    public static final AttachmentType<ChunkReinforcementData> REINFORCEMENT =
            AttachmentRegistry.<ChunkReinforcementData>builder()
                    .initializer(ChunkReinforcementData::new)
                    .persistent(ChunkReinforcementData.CODEC.codec())
                    .buildAndRegister(LoadBearing.id("reinforcement"));

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
