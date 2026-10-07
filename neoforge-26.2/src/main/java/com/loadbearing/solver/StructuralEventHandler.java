package com.loadbearing.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.loadbearing.Config;
import com.loadbearing.registry.LBAttachments;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

public final class StructuralEventHandler {
    private static final int CAVERN_SEED = 3;

    private StructuralEventHandler() {}

    public static void register(IEventBus bus) {
        bus.addListener(StructuralEventHandler::onPlace);
        bus.addListener(StructuralEventHandler::onMultiPlace);
        bus.addListener(StructuralEventHandler::onBreak);
        bus.addListener(StructuralEventHandler::onDetonate);
        bus.addListener(StructuralEventHandler::onPistonPre);
        bus.addListener(StructuralEventHandler::onPistonPost);
    }

    private static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled()) {
            return;
        }
        handlePlacement(event.getLevel(), event.getPos(), event.getEntity());
    }

    private static void onMultiPlace(BlockEvent.EntityMultiPlaceEvent event) {
        if (event.isCanceled()) {
            return;
        }
        event.getReplacedBlockSnapshots().forEach(snapshot ->
                handlePlacement(event.getLevel(), snapshot.getPos(), event.getEntity()));
    }

    private static void onBreak(BreakBlockEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        forget(level, pos);

        if (shouldSolveFor(event.getPlayer())) {
            SolverScheduler.get().request(level, pos);
        }
    }

    private static void onDetonate(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        List<BlockPos> affected = event.getAffectedBlocks();
        if (affected.isEmpty() || !Config.solverActive()) {
            return;
        }

        for (BlockPos pos : affected) {
            forget(level, pos);
        }
        solveAroundGaps(level, affected);
    }

    private static void solveAroundGaps(ServerLevel level, Collection<BlockPos> gaps) {
        LongSet empty = new LongOpenHashSet(gaps.size());
        for (BlockPos pos : gaps) {
            empty.add(pos.asLong());
        }
        LongSet seeds = new LongOpenHashSet();
        for (BlockPos pos : gaps) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbour = pos.relative(direction);
                if (empty.contains(neighbour.asLong()) || level.getBlockState(neighbour).isAir()) {
                    continue;
                }
                seeds.add(neighbour.asLong());
            }
        }
        for (long packed : seeds) {
            SolverScheduler.get().request(level, BlockPos.of(packed));
        }
    }

    private record PistonMove(Direction push, List<BlockPos> moved, List<BlockPos> destroyed) {}

    private static final Map<Long, PistonMove> PENDING_PISTONS = new ConcurrentHashMap<>();

    private static final int MAX_PENDING_PISTONS = 256;

    private static void onPistonPre(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !Config.solverActive()) {
            return;
        }
        Direction facing = event.getDirection();
        List<BlockPos> moved = new ArrayList<>();
        List<BlockPos> destroyed = new ArrayList<>();
        Direction push;

        if (event.getPistonMoveType().isExtend) {
            PistonStructureResolver resolver = event.getStructureHelper();
            if (resolver == null || !resolver.resolve()) {
                return;
            }
            push = resolver.getPushDirection();
            for (BlockPos pos : resolver.getToPush()) {
                moved.add(pos.immutable());
            }
            for (BlockPos pos : resolver.getToDestroy()) {
                destroyed.add(pos.immutable());
            }
        } else {
            push = facing.getOpposite();
            if (!level.getBlockState(event.getPos()).is(Blocks.STICKY_PISTON)) {
                return;
            }
            BlockPos pulled = event.getPos().relative(facing, 2);
            BlockState state = level.getBlockState(pulled);
            if (state.isAir() || state.getPistonPushReaction() != PushReaction.NORMAL) {
                return;
            }
            moved.add(pulled.immutable());
        }

        if (moved.isEmpty() && destroyed.isEmpty()) {
            return;
        }
        if (PENDING_PISTONS.size() > MAX_PENDING_PISTONS) {
            PENDING_PISTONS.clear();
        }
        PENDING_PISTONS.put(pistonKey(level, event.getPos()), new PistonMove(push, moved, destroyed));
    }

    private static void onPistonPost(PistonEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos piston = event.getPos();
        PistonMove move = PENDING_PISTONS.remove(pistonKey(level, piston));
        if (move == null || !Config.solverActive()) {
            return;
        }

        boolean[] wasPlaced = new boolean[move.moved().size()];
        boolean[] wasGrouted = new boolean[move.moved().size()];
        for (int i = 0; i < move.moved().size(); i++) {
            BlockPos from = move.moved().get(i);
            LevelChunk chunk = level.getChunkAt(from);
            wasPlaced[i] = chunk.getData(LBAttachments.PLACEMENT).isPlayerPlaced(from);

            wasGrouted[i] = chunk.getData(LBAttachments.REINFORCEMENT).isReinforced(from);
            forget(level, from);
        }
        for (BlockPos pos : move.destroyed()) {
            forget(level, pos);
        }

        for (BlockPos pos : move.moved()) {
            SolverScheduler.get().request(level, pos);
        }
        for (BlockPos pos : move.destroyed()) {
            SolverScheduler.get().request(level, pos);
        }
        for (int i = 0; i < move.moved().size(); i++) {
            BlockPos to = move.moved().get(i).relative(move.push());
            if (wasPlaced[i]) {
                markPlaced(level, to);
            }
            if (wasGrouted[i]) {
                LevelChunk chunk = level.getChunkAt(to);
                if (chunk.getData(LBAttachments.REINFORCEMENT).reinforce(to)) {
                    chunk.markUnsaved();
                    ReinforcementSender.broadcastOne(level, to.immutable());
                }
            }
            SolverScheduler.get().request(level, to);
        }
        SolverScheduler.get().request(level, piston);
    }

    private static long pistonKey(ServerLevel level, BlockPos pos) {
        return pos.asLong() * 31L + level.dimension().identifier().hashCode();
    }

    public static void clearPistonState() {
        PENDING_PISTONS.clear();
    }

    private static void handlePlacement(LevelAccessor accessor, BlockPos pos, Entity placer) {
        if (!(accessor instanceof ServerLevel level)) {
            return;
        }

        LevelChunk chunk = level.getChunkAt(pos);
        ChunkReinforcementData reinforcement = chunk.getData(LBAttachments.REINFORCEMENT);
        if (reinforcement.isReinforced(pos)) {
            reinforcement.clear(pos);
            ReinforcementSender.broadcastOne(level, pos.immutable());
        }
        markPlaced(level, pos);
        if (shouldSolveFor(placer)) {
            SolverScheduler.get().request(level, pos);
        }
    }

    private static boolean shouldSolveFor(Entity placer) {
        if (!Config.solverActive()) {
            return false;
        }
        if (placer instanceof Player player && player.isCreative() && !Config.appliesToCreative()) {
            return false;
        }
        return true;
    }

    public static void markPlaced(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = level.getChunkAt(pos);
        chunk.getData(LBAttachments.PLACEMENT).markPlaced(pos);
        chunk.markUnsaved();
        invalidateCacheAt(level, pos);
    }

    public static void forget(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = level.getChunkAt(pos);
        ChunkPlacementData placement = chunk.getData(LBAttachments.PLACEMENT);

        if (Config.TUNNEL_COLLAPSE.get() && !placement.isPlayerPlaced(pos)) {
            disturbAround(level, pos);
        }

        placement.clearPlaced(pos);

        ChunkReinforcementData reinforcement = chunk.getData(LBAttachments.REINFORCEMENT);
        if (reinforcement.isReinforced(pos)) {
            reinforcement.clear(pos);
            ReinforcementSender.broadcastOne(level, pos.immutable());
        }

        chunk.markUnsaved();
        invalidateCacheAt(level, pos);
    }

    private static void disturbAround(ServerLevel level, BlockPos pos) {
        LevelChunk here = level.getChunkAt(pos);
        if (here.getData(LBAttachments.DISTURBANCE).disturb(pos.immutable())) {
            here.markUnsaved();
        }
        for (Direction direction : Direction.values()) {
            disturb(level, pos.relative(direction));
        }
        if (level.getBlockState(pos.below()).isAir() && overWideVoid(level, pos)) {
            for (BlockPos ceiling : ceilingWithin(level, pos, CAVERN_SEED)) {
                disturb(level, ceiling);
            }
        }
    }

    private static void disturb(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).isAir()) {
            return;
        }
        LevelChunk chunk = level.getChunkAt(pos);
        if (chunk.getData(LBAttachments.PLACEMENT).isPlayerPlaced(pos)) {
            return;
        }
        if (chunk.getData(LBAttachments.DISTURBANCE).disturb(pos.immutable())) {
            chunk.markUnsaved();
            invalidateCacheAt(level, pos);
            SolverScheduler.get().request(level, pos.immutable());
        }
    }

    private static boolean isCeiling(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isAir();
    }

    private static boolean overWideVoid(ServerLevel level, BlockPos pos) {
        LongSet seen = new LongOpenHashSet();
        List<BlockPos> frontier = new ArrayList<>();
        seen.add(pos.asLong());
        frontier.add(pos.immutable());

        for (int step = 0; step < Config.CAVERN_SPAN.get(); step++) {
            List<BlockPos> next = new ArrayList<>();
            for (BlockPos at : frontier) {
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos side = at.relative(direction);
                    if (!level.getBlockState(side).isAir() && !level.getBlockState(side.below()).isAir()) {
                        return false;
                    }
                    if (isCeiling(level, side) && seen.add(side.asLong())) {
                        next.add(side.immutable());
                    }
                }
            }
            if (next.isEmpty()) {
                return false;
            }
            frontier = next;
        }
        return true;
    }

    private static List<BlockPos> ceilingWithin(ServerLevel level, BlockPos pos, int radius) {
        LongSet seen = new LongOpenHashSet();
        List<BlockPos> found = new ArrayList<>();
        List<BlockPos> frontier = new ArrayList<>();
        seen.add(pos.asLong());
        frontier.add(pos.immutable());

        for (int step = 0; step < radius; step++) {
            List<BlockPos> next = new ArrayList<>();
            for (BlockPos at : frontier) {
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos side = at.relative(direction);
                    if (isCeiling(level, side) && seen.add(side.asLong())) {
                        found.add(side.immutable());
                        next.add(side.immutable());
                    }
                }
            }
            frontier = next;
        }
        return found;
    }

    public static void invalidateCacheAt(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk != null) {
            chunk.getData(LBAttachments.SOLVER_CACHE).forget(pos.asLong());
            chunk.getData(LBAttachments.SOLVER_CACHE).bump();
        }
    }
}
