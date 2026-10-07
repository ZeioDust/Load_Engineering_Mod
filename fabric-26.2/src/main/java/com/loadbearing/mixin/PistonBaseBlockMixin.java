package com.loadbearing.mixin;

import com.loadbearing.solver.StructuralEventHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stands in for the piston pre and post events the other loaders provide. The head of moveBlocks
 * is the moment before anything moves, and a successful return is the moment after.
 */
@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockMixin {
    @Inject(method = "moveBlocks", at = @At("HEAD"))
    private void loadbearing$beforeMove(Level level, BlockPos pistonPos, Direction direction,
            boolean extending, CallbackInfoReturnable<Boolean> callback) {
        StructuralEventHandler.onPistonPre(level, pistonPos, direction, extending);
    }

    @Inject(method = "moveBlocks", at = @At("RETURN"))
    private void loadbearing$afterMove(Level level, BlockPos pistonPos, Direction direction,
            boolean extending, CallbackInfoReturnable<Boolean> callback) {
        StructuralEventHandler.onPistonPost(level, pistonPos);
    }
}
