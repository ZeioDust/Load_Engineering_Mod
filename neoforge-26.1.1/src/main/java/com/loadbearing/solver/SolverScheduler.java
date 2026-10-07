package com.loadbearing.solver;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.loadbearing.Config;
import com.loadbearing.LoadBearing;
import com.loadbearing.collapse.CollapseManager;
import com.loadbearing.registry.LBAttachments;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public final class SolverScheduler {
    private static final int COALESCE_CELL = 8;

    private static final int COALESCE_DELAY = 2;

    private static final int MAX_PENDING = 512;

    private static final SolverScheduler INSTANCE = new SolverScheduler();

    public static SolverScheduler get() {
        return INSTANCE;
    }

    private static final class Request {
        final ResourceKey<Level> level;
        final BlockPos origin;
        int readyAtTick;

        Request(ResourceKey<Level> level, BlockPos origin, int readyAtTick) {
            this.level = level;
            this.origin = origin;
            this.readyAtTick = readyAtTick;
        }
    }

    private final Deque<Request> pending = new ArrayDeque<>();
    private final Set<Long> pendingCells = new HashSet<>();
    private final Queue<SolverResult> completed = new ConcurrentLinkedQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger();
    private int tickCounter;
    private int dropped;
    private ExecutorService executor;

    private SolverScheduler() {}

    public void request(ServerLevel level, BlockPos origin) {
        if (!Config.solverActive()) {
            return;
        }
        long cell = cellKey(level.dimension(), origin);
        synchronized (this.pending) {
            if (this.pending.size() >= MAX_PENDING && !this.pendingCells.contains(cell)) {
                this.dropped++;
                return;
            }
            if (!this.pendingCells.add(cell)) {
                for (Request r : this.pending) {
                    if (r.level == level.dimension() && sameCell(r.origin, origin)) {
                        r.readyAtTick = this.tickCounter + COALESCE_DELAY;
                        return;
                    }
                }
                return;
            }
            this.pending.addLast(new Request(level.dimension(), origin.immutable(),
                    this.tickCounter + COALESCE_DELAY));
        }
    }

    public void runTickBudget(MinecraftServer server) {
        this.tickCounter++;
        applyCompleted(server);

        if (!Config.solverActive()) {
            return;
        }

        int budget = Config.SOLVER_OPS_PER_TICK.get();
        int spent = 0;

        while (spent < budget) {
            Request request = takeReady();
            if (request == null) {
                return;
            }
            ServerLevel level = server.getLevel(request.level);
            if (level == null) {
                continue;
            }
            SnapshotCapture capture = new SnapshotCapture(level, request.origin);
            ClusterSnapshot snapshot = capture.capture(budget - spent);
            spent += capture.operationsUsed();

            if (snapshot == null || snapshot.isEmpty()) {
                continue;
            }
            submit(snapshot);
        }
    }

    private Request takeReady() {
        synchronized (this.pending) {
            Request head = this.pending.peekFirst();
            if (head == null || head.readyAtTick > this.tickCounter) {
                return null;
            }
            this.pending.pollFirst();
            this.pendingCells.remove(cellKey(head.level, head.origin));
            return head;
        }
    }

    private void submit(ClusterSnapshot snapshot) {
        ExecutorService pool = pool();
        this.inFlight.incrementAndGet();
        CompletableFuture
                .supplyAsync(() -> StructuralSolver.solve(snapshot), pool)
                .whenComplete((result, error) -> {
                    this.inFlight.decrementAndGet();
                    if (error != null) {
                        LoadBearing.LOGGER.error("Structural solve failed", error);
                        return;
                    }
                    this.completed.add(result);
                });
    }

    private void applyCompleted(MinecraftServer server) {
        SolverResult result;
        while ((result = this.completed.poll()) != null) {
            ServerLevel level = levelFor(server, result.dimension());
            if (level == null) {
                continue;
            }
            cacheResult(level, result);

            if (Config.collapseActive()) {
                CollapseManager.get().onSolved(level, result);
            }
        }
    }

    private void cacheResult(ServerLevel level, SolverResult result) {
        Long2ObjectMap<ChunkSolverData> chunkCache = new Long2ObjectOpenHashMap<>();
        LongSet missing = new LongOpenHashSet();

        for (Long2DoubleMap.Entry entry : result.margins().long2DoubleEntrySet()) {
            long packed = entry.getLongKey();
            int chunkX = BlockPos.getX(packed) >> 4;
            int chunkZ = BlockPos.getZ(packed) >> 4;
            long chunkKey = ChunkPos.pack(chunkX, chunkZ);

            if (missing.contains(chunkKey)) {
                continue;
            }
            ChunkSolverData data = chunkCache.get(chunkKey);
            if (data == null) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    missing.add(chunkKey);
                    continue;
                }
                data = chunk.getData(LBAttachments.SOLVER_CACHE);
                data.invalidate();
                chunkCache.put(chunkKey, data);
            }
            data.record(packed, result.loads().get(packed), entry.getDoubleValue());
        }
    }

    private static ServerLevel levelFor(MinecraftServer server, String dimension) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().identifier().toString().equals(dimension)) {
                return level;
            }
        }
        return null;
    }

    private synchronized ExecutorService pool() {
        if (this.executor == null || this.executor.isShutdown()) {
            ThreadFactory factory = runnable -> {
                Thread thread = new Thread(runnable, "load-bearing-solver");
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY - 1);
                return thread;
            };
            this.executor = Executors.newFixedThreadPool(
                    Math.max(1, Runtime.getRuntime().availableProcessors() / 4), factory);
        }
        return this.executor;
    }

    public synchronized void shutdown() {
        synchronized (this.pending) {
            this.pending.clear();
            this.pendingCells.clear();
        }
        this.completed.clear();
        this.dropped = 0;
        if (this.executor != null) {
            this.executor.shutdownNow();
            this.executor = null;
        }
    }

    public int queueDepth() {
        synchronized (this.pending) {
            return this.pending.size();
        }
    }

    public int inFlight() {
        return this.inFlight.get();
    }

    public int droppedRequests() {
        synchronized (this.pending) {
            return this.dropped;
        }
    }

    private static boolean sameCell(BlockPos a, BlockPos b) {
        return a.getX() / COALESCE_CELL == b.getX() / COALESCE_CELL
                && a.getY() / COALESCE_CELL == b.getY() / COALESCE_CELL
                && a.getZ() / COALESCE_CELL == b.getZ() / COALESCE_CELL;
    }

    private static long cellKey(ResourceKey<Level> level, BlockPos pos) {
        long spatial = BlockPos.asLong(
                pos.getX() / COALESCE_CELL, pos.getY() / COALESCE_CELL, pos.getZ() / COALESCE_CELL);
        return spatial * 31L + level.identifier().hashCode();
    }
}
