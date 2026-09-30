package com.loadbearing.item;

import com.loadbearing.collapse.CollapseManager;
import com.loadbearing.material.MaterialProfile;
import com.loadbearing.material.MaterialRegistry;
import com.loadbearing.registry.LBAttachments;
import com.loadbearing.registry.LBSounds;
import com.loadbearing.solver.ChunkSolverData;
import com.loadbearing.solver.SolverScheduler;

import com.loadbearing.util.Messages;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public class StressWandItem extends Item {
    public StressWandItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getLevel() instanceof ServerLevel level) || context.getPlayer() == null) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        boolean grouted = level.getChunkAt(pos).getData(LBAttachments.REINFORCEMENT).isReinforced(pos);
        MaterialProfile base = MaterialRegistry.get(state);
        MaterialProfile profile = grouted ? base.reinforced() : base;

        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        double load = Double.NaN;
        double margin = Double.NaN;
        if (chunk != null) {
            ChunkSolverData data = chunk.getData(LBAttachments.SOLVER_CACHE);
            load = data.load(pos.asLong());
            margin = data.margin(pos.asLong());
        }

        Messages.tell(player, Component.translatable("message.loadbearing.wand_header",
                Component.translatable(state.getBlock().getDescriptionId())).withStyle(ChatFormatting.GOLD), false);

        if (Double.isNaN(margin)) {
            Messages.tell(player,
                    Component.translatable("message.loadbearing.wand_no_data").withStyle(ChatFormatting.GRAY), false);
            SolverScheduler.get().request(level, pos);
        } else {
            Messages.tell(player, Component.translatable("message.loadbearing.wand_load",
                    String.format("%.1f", load),
                    String.format("%.1f", profile.scaledStrength())), false);
            Messages.tell(player, Component.translatable("message.loadbearing.wand_span",
                    profile.scaledSpan()), false);
            Messages.tell(player, Component.translatable("message.loadbearing.wand_margin",
                    String.format("%.0f", margin)).withStyle(marginColour(margin)), false);
        }

        if (profile.tensionOnly()) {
            Messages.tell(player,
                    Component.translatable("message.loadbearing.wand_tension_only").withStyle(ChatFormatting.AQUA), false);
        }
        if (grouted) {
            Messages.tell(player, Component.translatable("message.loadbearing.wand_reinforced")
                    .withStyle(ChatFormatting.GREEN), false);
        }
        if (CollapseManager.get().isCondemned(level, pos)) {
            Messages.tell(player,
                    Component.translatable("message.loadbearing.wand_condemned").withStyle(ChatFormatting.DARK_RED), false);
        }

        level.playSound(null, pos, LBSounds.GAUGE_BEEP.get(), SoundSource.PLAYERS, 0.5F, 1.2F);
        context.getItemInHand().hurtAndBreak(1, (ServerLevel) level, player, item -> {});
        return InteractionResult.SUCCESS_SERVER;
    }

    public static ChatFormatting marginColour(double margin) {
        if (margin >= 60.0D) {
            return ChatFormatting.GREEN;
        }
        if (margin >= 20.0D) {
            return ChatFormatting.YELLOW;
        }
        if (margin >= 5.0D) {
            return ChatFormatting.GOLD;
        }
        return ChatFormatting.RED;
    }
}
