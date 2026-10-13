package com.loadbearing.item;

import com.loadbearing.registry.LBAttachments;
import com.loadbearing.registry.LBParticles;
import com.loadbearing.registry.LBSounds;
import com.loadbearing.solver.ChunkReinforcementData;
import com.loadbearing.solver.ReinforcementSender;
import com.loadbearing.solver.SolverScheduler;
import com.loadbearing.solver.StructuralEventHandler;
import com.loadbearing.util.Messages;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

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

public class ReinforcingGroutItem extends Item {
    public ReinforcingGroutItem(Item.Properties properties) {
        super(properties);
    }

    private static boolean standsOnGround(ServerLevel level, BlockPos from) {
        LongSet seen = new LongOpenHashSet();
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        seen.add(from.asLong());
        queue.enqueue(from.asLong());
        int budget = 512;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        while (!queue.isEmpty() && budget-- > 0) {
            long packed = queue.dequeueLong();
            int x = BlockPos.getX(packed);
            int y = BlockPos.getY(packed);
            int z = BlockPos.getZ(packed);
            for (int dy = -1; dy <= 1; dy += 2) {
                int ny = y + dy;
                if (ny < level.getMinY() || ny > level.getMaxY()) {
                    continue;
                }
                cursor.set(x, ny, z);
                if (level.getBlockState(cursor).isAir()) {
                    continue;
                }

                if (!LBAttachments.placement(level.getChunkAt(cursor)).isPlayerPlaced(cursor)) {
                    return true;
                }
                long next = BlockPos.asLong(x, ny, z);
                if (seen.add(next)) {
                    queue.enqueue(next);
                }
            }
        }
        return false;
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

        if (state.isAir()) {
            return InteractionResult.PASS;
        }

        if (state.getDestroySpeed(level, pos) < 0.0F) {
            Messages.tell(player, Component.translatable("message.loadbearing.grout_refuses")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        if (!standsOnGround(level, pos)) {
            Messages.tell(player, Component.translatable("message.loadbearing.grout_not_foundation")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        LevelChunk chunk = level.getChunkAt(pos);
        ChunkReinforcementData data = LBAttachments.reinforcement(chunk);
        if (!data.reinforce(pos)) {
            Messages.tell(player, Component.translatable("message.loadbearing.grout_already")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.FAIL;
        }
        chunk.markUnsaved();

        StructuralEventHandler.invalidateCacheAt(level, pos);
        SolverScheduler.get().request(level, pos);
        ReinforcementSender.broadcastOne(level, pos);

        level.playSound(null, pos, LBSounds.CONCRETE_POUR.get(), SoundSource.BLOCKS, 0.8F, 1.1F);
        level.sendParticles(LBParticles.CONCRETE_DUST.get(),
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 12, 0.4D, 0.4D, 0.4D, 0.01D);
        Messages.tell(player, Component.translatable("message.loadbearing.grout_applied")
                .withStyle(ChatFormatting.GREEN), true);

        if (!player.isCreative()) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
