package com.loadbearing.command;

import java.util.ArrayList;
import java.util.List;

import com.loadbearing.registry.LBAttachments;
import com.loadbearing.solver.ChunkReinforcementData;
import com.loadbearing.solver.ReinforcementSender;
import com.loadbearing.solver.SolverScheduler;
import com.loadbearing.solver.StructuralEventHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class SelfTest {
    private static final SelfTest INSTANCE = new SelfTest();

    public static SelfTest get() {
        return INSTANCE;
    }

    private static final int SETTLE_TICKS = 260;

    private static final int LIFT = 40;

    private static final int PAD = 112;

    private enum Phase { IDLE, SETTLING, DONE }

    private record Check(String name, BlockPos pos, boolean expectPresent) {}

    private final List<BlockPos> placed = new ArrayList<>();

    private Phase phase = Phase.IDLE;
    private int ticksLeft;
    private ServerLevel level;
    private CommandSourceStack source;
    private BlockPos origin;
    private final List<Check> checks = new ArrayList<>();

    private SelfTest() {}

    public boolean running() {
        return this.phase != Phase.IDLE;
    }

    public int start(ServerLevel level, CommandSourceStack source, BlockPos near) {
        this.level = level;
        this.source = source;
        this.origin = new BlockPos(near.getX(), Math.min(near.getY() + LIFT, level.getMaxY() - 20),
                near.getZ());
        this.checks.clear();
        this.placed.clear();

        if (!volumeIsEmpty()) {
            return -1;
        }
        buildPad();

        cantilever(0, false, false);
        cantilever(12, true, false);
        cantilever(24, false, true);
        floatingBlock(36);
        column(42);
        tunnel(48, 1, false);
        tunnel(56, 3, false);
        tunnel(64, 3, true);
        cave(72, 5);
        cave(86, 21);

        this.phase = Phase.SETTLING;
        this.ticksLeft = SETTLE_TICKS;
        return this.checks.size();
    }

    public void tick(MinecraftServer server) {
        if (this.phase != Phase.SETTLING) {
            return;
        }
        if (--this.ticksLeft > 0) {
            return;
        }

        int passed = 0;
        List<String> failures = new ArrayList<>();
        for (Check check : this.checks) {
            boolean present = !this.level.getBlockState(check.pos()).isAir();
            if (present == check.expectPresent()) {
                passed++;
            } else {
                failures.add(check.name());
            }
        }

        int total = this.checks.size();
        int finalPassed = passed;
        this.source.sendSuccess(() -> Component.literal(
                "Load Bearing self test: " + finalPassed + " of " + total + " checks passed")
                .withStyle(failures.isEmpty() ? ChatFormatting.GREEN : ChatFormatting.RED), false);
        for (String name : failures) {
            this.source.sendSuccess(() -> Component.literal("  FAILED: " + name)
                    .withStyle(ChatFormatting.RED), false);
        }

        removeWhatWeBuilt();
        this.phase = Phase.IDLE;
        this.checks.clear();
        this.placed.clear();
        this.level = null;
        this.source = null;
    }

    public void cancel() {
        this.phase = Phase.IDLE;
        this.checks.clear();
        this.placed.clear();
        this.level = null;
        this.source = null;
    }

    private void cantilever(int offsetX, boolean groutPier, boolean groutDeck) {
        BlockPos base = this.origin.offset(offsetX, 1, 0);
        for (int dy = 0; dy < 6; dy++) {
            set(base.above(dy));
        }
        BlockPos top = base.above(5);
        for (int dx = 1; dx <= 8; dx++) {
            set(top.offset(dx, 0, 0));
        }

        if (groutPier) {
            grout(base);
        }
        if (groutDeck) {
            grout(top.offset(2, 0, 0));
        }
        mark(base, top.offset(8, 0, 0));

        int expected = groutPier ? 6 : 3;
        String label = groutPier ? "grouted pier reaches six"
                : groutDeck ? "grout on the deck changes nothing" : "plain pier reaches three";
        check(label + " (last kept)", top.offset(expected, 0, 0), true);
        check(label + " (first lost)", top.offset(expected + 1, 0, 0), false);
    }

    private void floatingBlock(int offsetX) {
        BlockPos pos = this.origin.offset(offsetX, 6, 0);
        set(pos);
        mark(pos, pos);
        check("a block in mid air falls", pos, false);
    }

    private void column(int offsetX) {
        BlockPos base = this.origin.offset(offsetX, 1, 0);
        for (int dy = 0; dy < 10; dy++) {
            set(base.above(dy));
        }
        mark(base, base.above(9));
        check("a grounded column stands", base.above(9), true);
    }

    private void tunnel(int offsetX, int width, boolean groutRoof) {
        BlockPos corner = this.origin.offset(offsetX, 1, -3);
        for (int dx = -1; dx <= width; dx++) {
            for (int dy = 0; dy <= 3; dy++) {
                for (int dz = 0; dz <= 6; dz++) {
                    setRock(corner.offset(dx, dy, dz));
                }
            }
        }

        BlockPos roof = corner.offset(0, 2, 0);
        if (groutRoof) {
            for (int dx = 0; dx < width; dx++) {
                for (int dz = 1; dz <= 5; dz++) {
                    grout(roof.offset(dx, 0, dz));
                }
            }
        }

        for (int dx = 0; dx < width; dx++) {
            for (int dz = 1; dz <= 5; dz++) {
                BlockPos pos = corner.offset(dx, 1, dz);
                this.level.removeBlock(pos, false);
                StructuralEventHandler.forget(this.level, pos);
            }
        }
        SolverScheduler.get().request(this.level, corner.offset(0, 1, 3));

        boolean expectRoof = width <= 2 || groutRoof;
        String label = width + " wide tunnel" + (groutRoof ? " with a grouted roof" : "");
        check(label + (expectRoof ? " holds" : " caves in"),
                roof.offset(width / 2, 0, 3), expectRoof);
    }

    private void cave(int offsetX, int width) {
        BlockPos corner = this.origin.offset(offsetX, 1, -(width / 2));
        for (int dx = -1; dx <= width; dx++) {
            for (int dy = 0; dy <= 4; dy++) {
                for (int dz = -1; dz <= width; dz++) {
                    setRock(corner.offset(dx, dy, dz));
                }
            }
        }
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < width; dz++) {
                this.level.setBlock(corner.offset(dx, 1, dz), Blocks.AIR.defaultBlockState(), 2);
                this.level.setBlock(corner.offset(dx, 2, dz), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        BlockPos middle = corner.offset(width / 2, 3, width / 2);
        this.level.removeBlock(middle, false);
        StructuralEventHandler.forget(this.level, middle);
        SolverScheduler.get().request(this.level, middle);

        boolean expectHold = width < 13;
        check("a " + width + " wide cave " + (expectHold ? "survives" : "caves in")
                + " one broken ceiling block",
                corner.offset(width / 2 - 2, 3, width / 2), expectHold);
    }

    private void set(BlockPos pos) {
        this.level.setBlock(pos, Blocks.SANDSTONE.defaultBlockState(), 2);
        this.placed.add(pos.immutable());
    }

    private void setRock(BlockPos pos) {
        this.level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
        this.placed.add(pos.immutable());
    }

    private void grout(BlockPos pos) {
        LevelChunk chunk = this.level.getChunkAt(pos);
        ChunkReinforcementData data = chunk.getData(LBAttachments.REINFORCEMENT);
        if (data.reinforce(pos)) {
            chunk.markUnsaved();
            ReinforcementSender.broadcastOne(this.level, pos.immutable());
        }
    }

    private void mark(BlockPos from, BlockPos to) {
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (!this.level.getBlockState(pos).isAir()) {
                StructuralEventHandler.markPlaced(this.level, pos.immutable());
            }
        }
        SolverScheduler.get().request(this.level, from.immutable());
    }

    private void check(String name, BlockPos pos, boolean expectPresent) {
        this.checks.add(new Check(name, pos.immutable(), expectPresent));
    }

    private boolean volumeIsEmpty() {
        for (int x = -2; x <= PAD + 12; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 0; y <= 16; y++) {
                    if (!this.level.getBlockState(this.origin.offset(x, y, z)).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void removeWhatWeBuilt() {
        BlockState air = Blocks.AIR.defaultBlockState();
        for (BlockPos pos : this.placed) {
            if (!this.level.getBlockState(pos).isAir()) {
                this.level.setBlock(pos, air, 2);
            }
            StructuralEventHandler.forget(this.level, pos);
        }
    }

    private void buildPad() {
        BlockState stone = Blocks.STONE.defaultBlockState();
        for (int x = -2; x <= PAD + 12; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos pos = this.origin.offset(x, 0, z);
                this.level.setBlock(pos, stone, 2);
                this.placed.add(pos.immutable());
            }
        }
    }
}
