package com.loadbearing.registry;

import java.util.function.Supplier;

import com.loadbearing.LoadBearing;
import com.loadbearing.solver.ChunkDisturbanceData;
import com.loadbearing.solver.ChunkPlacementData;
import com.loadbearing.solver.ChunkReinforcementData;
import com.loadbearing.solver.ChunkSolverData;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class LBAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTRY =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, LoadBearing.MODID);

    private LBAttachments() {}

    public static final Supplier<AttachmentType<ChunkPlacementData>> PLACEMENT =
            REGISTRY.register("placement", () -> AttachmentType
                    .<ChunkPlacementData>builder(() -> new ChunkPlacementData())
                    .serialize(ChunkPlacementData.CODEC)
                    .build());

    public static final Supplier<AttachmentType<ChunkSolverData>> SOLVER_CACHE =
            REGISTRY.register("solver_cache", () -> AttachmentType
                    .<ChunkSolverData>builder(() -> new ChunkSolverData())
                    .build());

    public static final Supplier<AttachmentType<ChunkDisturbanceData>> DISTURBANCE =
            REGISTRY.register("disturbance", () -> AttachmentType
                    .<ChunkDisturbanceData>builder(() -> new ChunkDisturbanceData())
                    .serialize(ChunkDisturbanceData.CODEC)
                    .build());

    public static final Supplier<AttachmentType<ChunkReinforcementData>> REINFORCEMENT =
            REGISTRY.register("reinforcement", () -> AttachmentType
                    .<ChunkReinforcementData>builder(() -> new ChunkReinforcementData())
                    .serialize(ChunkReinforcementData.CODEC)
                    .build());
}
