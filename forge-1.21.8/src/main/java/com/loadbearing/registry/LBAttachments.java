package com.loadbearing.registry;

import com.loadbearing.LoadBearing;
import com.loadbearing.solver.ChunkDisturbanceData;
import com.loadbearing.solver.ChunkPlacementData;
import com.loadbearing.solver.ChunkReinforcementData;
import com.loadbearing.solver.ChunkSolverData;
import com.mojang.serialization.Codec;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jspecify.annotations.Nullable;

/**
 * This loader has no data attachments, so the same four per-chunk objects live in one capability
 * attached to every LevelChunk. The stored shape is identical: each sub-object is written with the
 * same codec under the same key, so a world keeps its placement, disturbance and reinforcement
 * records exactly as it does on the attachment-based targets.
 */
public final class LBAttachments {
    public static final Capability<ChunkData> CHUNK_DATA =
            CapabilityManager.get(new CapabilityToken<ChunkData>() {});

    private LBAttachments() {}

    public static void attach(AttachCapabilitiesEvent.LevelChunks event) {
        event.addCapability(LoadBearing.id("chunk_data"), new Provider());
    }

    public static ChunkPlacementData placement(ChunkAccess chunk) {
        return of(chunk).placement;
    }

    public static ChunkSolverData solverCache(ChunkAccess chunk) {
        return of(chunk).solverCache;
    }

    public static ChunkDisturbanceData disturbance(ChunkAccess chunk) {
        return of(chunk).disturbance;
    }

    public static ChunkReinforcementData reinforcement(ChunkAccess chunk) {
        return of(chunk).reinforcement;
    }

    private static ChunkData of(ChunkAccess chunk) {
        if (chunk instanceof LevelChunk levelChunk) {
            return levelChunk.getCapability(CHUNK_DATA)
                    .orElseThrow(() -> new IllegalStateException(
                            "Load Bearing chunk data missing from " + chunk.getPos()));
        }
        throw new IllegalStateException("Load Bearing chunk data requested for a proto chunk at "
                + chunk.getPos());
    }

    public static final class ChunkData {
        public ChunkPlacementData placement = new ChunkPlacementData();

        public ChunkSolverData solverCache = new ChunkSolverData();

        public ChunkDisturbanceData disturbance = new ChunkDisturbanceData();

        public ChunkReinforcementData reinforcement = new ChunkReinforcementData();
    }

    private static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private static final Codec<ChunkPlacementData> PLACEMENT = ChunkPlacementData.CODEC.codec();
        private static final Codec<ChunkDisturbanceData> DISTURBANCE =
                ChunkDisturbanceData.CODEC.codec();
        private static final Codec<ChunkReinforcementData> REINFORCEMENT =
                ChunkReinforcementData.CODEC.codec();

        private final ChunkData data = new ChunkData();

        private final LazyOptional<ChunkData> handle = LazyOptional.of(() -> this.data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
            return CHUNK_DATA.orEmpty(capability, this.handle);
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider registryAccess) {
            CompoundTag tag = new CompoundTag();
            tag.put("placement",
                    PLACEMENT.encodeStart(NbtOps.INSTANCE, this.data.placement).getOrThrow());
            tag.put("disturbance",
                    DISTURBANCE.encodeStart(NbtOps.INSTANCE, this.data.disturbance).getOrThrow());
            tag.put("reinforcement",
                    REINFORCEMENT.encodeStart(NbtOps.INSTANCE, this.data.reinforcement).getOrThrow());
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag tag) {
            this.data.placement = read(PLACEMENT, tag.get("placement"), new ChunkPlacementData());
            this.data.disturbance =
                    read(DISTURBANCE, tag.get("disturbance"), new ChunkDisturbanceData());
            this.data.reinforcement =
                    read(REINFORCEMENT, tag.get("reinforcement"), new ChunkReinforcementData());
        }

        private static <T> T read(Codec<T> codec, @Nullable Tag tag, T fallback) {
            if (tag == null) {
                return fallback;
            }
            return codec.parse(NbtOps.INSTANCE, tag).result().orElse(fallback);
        }
    }
}
