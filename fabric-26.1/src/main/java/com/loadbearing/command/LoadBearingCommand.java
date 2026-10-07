package com.loadbearing.command;

import com.loadbearing.Config;
import com.loadbearing.collapse.CollapseManager;
import com.loadbearing.material.MaterialProfile;
import com.loadbearing.material.MaterialRegistry;
import com.loadbearing.registry.LBAttachments;
import com.loadbearing.solver.ChunkReinforcementData;
import com.loadbearing.solver.ChunkSolverData;
import com.loadbearing.solver.ReinforcementSender;
import com.loadbearing.solver.SolverExplanation;
import com.loadbearing.solver.SolverScheduler;
import com.loadbearing.solver.StructuralEventHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class LoadBearingCommand {
    private LoadBearingCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("loadbearing")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));

        root.then(Commands.literal("status").executes(context -> status(context.getSource())));

        root.then(Commands.literal("check")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> check(
                                context.getSource(),
                                BlockPosArgument.getLoadedBlockPos(context, "pos")))));

        root.then(Commands.literal("explain")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> explain(
                                context.getSource(),
                                BlockPosArgument.getLoadedBlockPos(context, "pos")))));

        root.then(Commands.literal("mark")
                .then(Commands.argument("from", BlockPosArgument.blockPos())
                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                                .executes(context -> mark(
                                        context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "from"),
                                        BlockPosArgument.getLoadedBlockPos(context, "to"),
                                        true)))));

        root.then(Commands.literal("unmark")
                .then(Commands.argument("from", BlockPosArgument.blockPos())
                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                                .executes(context -> mark(
                                        context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "from"),
                                        BlockPosArgument.getLoadedBlockPos(context, "to"),
                                        false)))));

        root.then(Commands.literal("reinforce")
                .then(Commands.argument("from", BlockPosArgument.blockPos())
                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                                .executes(context -> reinforce(
                                        context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "from"),
                                        BlockPosArgument.getLoadedBlockPos(context, "to"),
                                        true)))));

        root.then(Commands.literal("unreinforce")
                .then(Commands.argument("from", BlockPosArgument.blockPos())
                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                                .executes(context -> reinforce(
                                        context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "from"),
                                        BlockPosArgument.getLoadedBlockPos(context, "to"),
                                        false)))));

        root.then(Commands.literal("solve")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> solve(
                                context.getSource(),
                                BlockPosArgument.getLoadedBlockPos(context, "pos")))));

        root.then(Commands.literal("dig")
                .then(Commands.argument("from", BlockPosArgument.blockPos())
                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                                .executes(context -> dig(
                                        context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "from"),
                                        BlockPosArgument.getLoadedBlockPos(context, "to"))))));

        root.then(Commands.literal("selftest")
                .executes(context -> selfTest(context.getSource())));

        dispatcher.register(root);
    }

    private static int status(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("command.loadbearing.status_header")
                .withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.translatable("command.loadbearing.status_enabled",
                Config.SYSTEM_ENABLED.get(), Config.DIFFICULTY_MODE.get().name()), false);
        source.sendSuccess(() -> Component.translatable("command.loadbearing.status_queue",
                SolverScheduler.get().queueDepth(), SolverScheduler.get().inFlight(),
                SolverScheduler.get().droppedRequests()), false);
        source.sendSuccess(() -> Component.translatable("command.loadbearing.status_pending",
                CollapseManager.get().pendingCount()), false);
        source.sendSuccess(() -> Component.translatable("command.loadbearing.status_budget",
                Config.SOLVER_OPS_PER_TICK.get(), Config.SOLVER_RADIUS.get(),
                Config.SOLVER_MAX_NODES.get()), false);
        return 1;
    }

    private static int dig(CommandSourceStack source, BlockPos from, BlockPos to) {
        ServerLevel level = source.getLevel();
        int removed = 0;
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) {
                continue;
            }
            BlockPos at = pos.immutable();
            level.removeBlock(at, false);
            StructuralEventHandler.forget(level, at);
            removed++;
        }
        SolverScheduler.get().request(level, from.immutable());
        int count = removed;
        source.sendSuccess(() -> Component.translatable("command.loadbearing.dug", count), false);
        return count;
    }

    private static int selfTest(CommandSourceStack source) {
        if (SelfTest.get().running()) {
            source.sendFailure(Component.literal("A self test is already running."));
            return 0;
        }
        BlockPos near = BlockPos.containing(source.getPosition());
        int checks = SelfTest.get().start(source.getLevel(), source, near);
        if (checks < 0) {
            source.sendFailure(Component.literal(
                    "No clear space forty blocks above you. Move somewhere open and try again."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(
                "Self test running: " + checks + " checks, answer in a few seconds.")
                .withStyle(ChatFormatting.GRAY), false);
        return checks;
    }

    private static int check(CommandSourceStack source, BlockPos pos) {
        ServerLevel level = source.getLevel();
        BlockState state = level.getBlockState(pos);

        boolean grouted = LBAttachments.reinforcement(level.getChunkAt(pos)).isReinforced(pos);
        MaterialProfile base = MaterialRegistry.get(state);
        MaterialProfile profile = grouted ? base.reinforced() : base;

        source.sendSuccess(() -> Component.translatable("command.loadbearing.check_header",
                Component.translatable(state.getBlock().getDescriptionId()),
                pos.toShortString()).withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.translatable("command.loadbearing.check_profile",
                String.format("%.2f", profile.scaledWeight()),
                String.format("%.1f", profile.scaledStrength()),
                profile.scaledSpan(),
                String.format("%.1f", profile.scaledSoilBearing())), false);
        source.sendSuccess(() -> Component.translatable("command.loadbearing.check_flags",
                profile.tensionOnly(), profile.brittle()), false);

        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) {
            source.sendFailure(Component.translatable("command.loadbearing.check_unloaded"));
            return 0;
        }

        boolean placed = LBAttachments.placement(chunk).isPlayerPlaced(pos);
        source.sendSuccess(() -> Component.translatable("command.loadbearing.check_placed", placed), false);

        ChunkSolverData data = LBAttachments.solverCache(chunk);
        double margin = data.margin(pos.asLong());
        if (Double.isNaN(margin)) {
            source.sendSuccess(() -> Component.translatable("command.loadbearing.check_no_data"), false);
        } else {
            source.sendSuccess(() -> Component.translatable("command.loadbearing.check_result",
                    String.format("%.2f", data.load(pos.asLong())),
                    String.format("%.0f", margin)), false);
        }

        if (grouted) {
            source.sendSuccess(() -> Component.translatable("command.loadbearing.check_reinforced")
                    .withStyle(ChatFormatting.GREEN), false);
        }
        if (LBAttachments.disturbance(level.getChunkAt(pos)).isDisturbed(pos)) {
            source.sendSuccess(() -> Component.translatable("command.loadbearing.check_disturbed")
                    .withStyle(ChatFormatting.YELLOW), false);
        }
        if (CollapseManager.get().isCondemned(level, pos)) {
            source.sendSuccess(() -> Component.translatable("command.loadbearing.check_condemned")
                    .withStyle(ChatFormatting.DARK_RED), false);
        }
        return 1;
    }

    private static int reinforce(CommandSourceStack source, BlockPos from, BlockPos to, boolean on) {
        ServerLevel level = source.getLevel();
        int touched = 0;
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (level.getBlockState(pos).isAir()) {
                continue;
            }
            LevelChunk chunk = level.getChunkAt(pos);
            ChunkReinforcementData data = LBAttachments.reinforcement(chunk);
            if (on) {
                if (!data.reinforce(pos)) {
                    continue;
                }
            } else {
                if (!data.isReinforced(pos)) {
                    continue;
                }
                data.clear(pos);
            }
            chunk.markUnsaved();
            StructuralEventHandler.invalidateCacheAt(level, pos);
            ReinforcementSender.broadcastOne(level, pos.immutable());
            touched++;
        }
        SolverScheduler.get().request(level, from);
        int count = touched;
        source.sendSuccess(() -> Component.translatable(
                on ? "command.loadbearing.reinforced" : "command.loadbearing.unreinforced", count), false);
        return count;
    }

    private static int mark(CommandSourceStack source, BlockPos from, BlockPos to, boolean placed) {
        ServerLevel level = source.getLevel();
        int touched = 0;
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (level.getBlockState(pos).isAir()) {
                continue;
            }
            if (placed) {
                StructuralEventHandler.markPlaced(level, pos);
            } else {
                StructuralEventHandler.forget(level, pos);
            }
            touched++;
        }
        SolverScheduler.get().request(level, from);
        int count = touched;
        source.sendSuccess(() -> Component.translatable(
                placed ? "command.loadbearing.marked" : "command.loadbearing.unmarked", count), false);
        return count;
    }

    private static int explain(CommandSourceStack source, BlockPos pos) {
        ServerLevel level = source.getLevel();
        SolverExplanation explanation = SolverExplanation.of(level, pos);

        source.sendSuccess(() -> Component.literal("-- solver verdict at " + pos.toShortString() + " --")
                .withStyle(ChatFormatting.GOLD), false);
        for (String line : explanation.lines()) {
            source.sendSuccess(() -> Component.literal(line), false);
        }
        for (BlockPos anchor : SolverExplanation.sampleAnchors(level, pos, 5)) {
            source.sendSuccess(() -> Component.literal("  anchor at " + anchor.toShortString()
                    + " (" + level.getBlockState(anchor).getBlock().getDescriptionId() + ")")
                    .withStyle(ChatFormatting.GRAY), false);
        }
        return 1;
    }

    private static int solve(CommandSourceStack source, BlockPos pos) {
        SolverScheduler.get().request(source.getLevel(), pos);
        source.sendSuccess(() -> Component.translatable(
                "command.loadbearing.solve_queued", pos.toShortString()), false);
        return 1;
    }
}
