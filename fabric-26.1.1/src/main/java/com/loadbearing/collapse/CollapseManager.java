package com.loadbearing.collapse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.loadbearing.Config;
import com.loadbearing.advancement.LBCriteria;
import com.loadbearing.entity.DustCloudEntity;
import com.loadbearing.entity.FallingDebrisEntity;
import com.loadbearing.registry.LBParticles;
import com.loadbearing.registry.LBSounds;
import com.loadbearing.solver.SolverResult;
import com.loadbearing.solver.StructuralEventHandler;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class CollapseManager {
    private static final CollapseManager INSTANCE = new CollapseManager();

    public static CollapseManager get() {
        return INSTANCE;
    }

    private static final class Condemned {
        final BlockPos pos;
        final boolean crushed;
        int ticksLeft;

        final int overlayId;

        Condemned(BlockPos pos, boolean crushed, int ticksLeft, int overlayId) {
            this.pos = pos;
            this.crushed = crushed;
            this.ticksLeft = ticksLeft;
            this.overlayId = overlayId;
        }
    }

    private final Map<ResourceKey<Level>, Long2ObjectMap<Condemned>> condemned = new HashMap<>();
    private int nextOverlayId = 0x4C42_0000;

    private CollapseManager() {}

    public void onSolved(ServerLevel level, SolverResult result) {
        Long2ObjectMap<Condemned> map = this.condemned.computeIfAbsent(
                level.dimension(), k -> new Long2ObjectOpenHashMap<>());

        List<Long> reprieved = new ArrayList<>();
        for (Long2ObjectMap.Entry<Condemned> entry : map.long2ObjectEntrySet()) {
            long packed = entry.getLongKey();
            if (result.margins().containsKey(packed) && result.margins().get(packed) > 0.0D) {
                reprieved.add(packed);
            }
        }
        for (long packed : reprieved) {
            Condemned c = map.remove(packed);
            if (c != null) {
                level.destroyBlockProgress(c.overlayId, c.pos, -1);
            }
        }

        int delay = Config.COLLAPSE_DELAY_TICKS.get();
        LongList failures = result.failures();
        int cap = Config.MAX_BLOCKS_PER_COLLAPSE.get();
        int condemnedThisEvent = 0;

        for (int i = 0; i < failures.size() && condemnedThisEvent < cap; i++) {
            long packed = failures.getLong(i);
            if (map.containsKey(packed)) {
                continue;
            }
            BlockPos pos = BlockPos.of(packed);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) {
                continue;
            }

            boolean crushed = result.crushed().contains(packed);
            int overlayId = this.nextOverlayId++;
            map.put(packed, new Condemned(pos, crushed, delay, overlayId));
            condemnedThisEvent++;

            if (Config.CRACK_WARNING_ENABLED.get() && delay > 0) {
                level.destroyBlockProgress(overlayId, pos, 6);
                level.playSound(null, pos, LBSounds.CRACK_WARNING.get(), SoundSource.BLOCKS, 0.7F,
                        0.9F + level.getRandom().nextFloat() * 0.2F);
            }
        }

        if (condemnedThisEvent > 0) {
            BlockPos origin = BlockPos.of(result.origin());
            level.playSound(null, origin, LBSounds.STRESS_CREAK.get(), SoundSource.BLOCKS, 0.6F, 0.8F);
        }
    }

    public void tick(MinecraftServer server) {
        if (this.condemned.isEmpty()) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            Long2ObjectMap<Condemned> map = this.condemned.get(level.dimension());
            if (map == null || map.isEmpty()) {
                continue;
            }
            tickLevel(level, map);
        }
    }

    private void tickLevel(ServerLevel level, Long2ObjectMap<Condemned> map) {
        List<Condemned> due = new ArrayList<>();
        for (Long2ObjectMap.Entry<Condemned> entry : map.long2ObjectEntrySet()) {
            Condemned c = entry.getValue();
            if (--c.ticksLeft <= 0) {
                due.add(c);
            } else if (Config.CRACK_WARNING_ENABLED.get()) {
                int total = Math.max(1, Config.COLLAPSE_DELAY_TICKS.get());
                int stage = 4 + (int) ((1.0D - (double) c.ticksLeft / total) * 5.0D);
                level.destroyBlockProgress(c.overlayId, c.pos, Math.min(9, stage));
            }
        }
        if (due.isEmpty()) {
            return;
        }

        int cap = Config.MAX_BLOCKS_PER_COLLAPSE.get();
        int dropped = 0;
        BlockPos origin = due.get(0).pos;

        for (Condemned c : due) {
            map.remove(c.pos.asLong());
            level.destroyBlockProgress(c.overlayId, c.pos, -1);
            if (dropped >= cap) {
                continue;
            }
            if (collapse(level, c.pos, c.crushed)) {
                dropped++;
            }
        }

        if (dropped > 0) {
            announce(level, origin, dropped);
        }
    }

    public boolean collapse(ServerLevel level, BlockPos pos, boolean crushed) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        if (state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }

        level.removeBlock(pos, false);
        StructuralEventHandler.forget(level, pos);

        FallingDebrisEntity debris = FallingDebrisEntity.fall(level, pos, state, crushed);
        if (debris == null) {
            return false;
        }

        level.sendParticles(LBParticles.DEBRIS_CHIP.get(),
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                6, 0.3D, 0.3D, 0.3D, 0.02D);

        com.loadbearing.solver.SolverScheduler.get().request(level, pos);
        return true;
    }

    private void announce(ServerLevel level, BlockPos origin, int count) {
        float volume = Math.min(1.6F, 0.5F + count * 0.05F);
        level.playSound(null, origin, LBSounds.COLLAPSE_RUMBLE.get(), SoundSource.BLOCKS, volume, 0.7F);

        DustCloudEntity cloud = DustCloudEntity.create(level, origin, Math.min(6.0F, 1.5F + count * 0.15F));
        if (cloud != null) {
            level.addFreshEntity(cloud);
        }
        level.sendParticles(LBParticles.CONCRETE_DUST.get(),
                origin.getX() + 0.5D, origin.getY() + 0.5D, origin.getZ() + 0.5D,
                Math.min(120, 10 + count * 3), 1.5D, 1.0D, 1.5D, 0.05D);

        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerThan(origin, 48.0D)) {
                LBCriteria.STRUCTURAL.trigger(player, LBCriteria.Kind.IT_DID_NOT_HOLD);
            }
        }
    }

    public boolean isCondemned(ServerLevel level, BlockPos pos) {
        Long2ObjectMap<Condemned> map = this.condemned.get(level.dimension());
        return map != null && map.containsKey(pos.asLong());
    }

    public int pendingCount() {
        int total = 0;
        for (Long2ObjectMap<Condemned> map : this.condemned.values()) {
            total += map.size();
        }
        return total;
    }

    public void clear() {
        this.condemned.clear();
    }
}
