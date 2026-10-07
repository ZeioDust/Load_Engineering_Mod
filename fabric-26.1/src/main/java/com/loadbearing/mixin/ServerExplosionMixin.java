package com.loadbearing.mixin;

import java.util.List;

import com.loadbearing.solver.StructuralEventHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stands in for the explosion-detonate event the other loaders provide. The list returned here is
 * exactly the set of blocks the explosion is about to remove.
 */
@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
    @Shadow
    private ServerLevel level;

    @Inject(method = "calculateExplodedPositions", at = @At("RETURN"))
    private void loadbearing$afterCalculate(CallbackInfoReturnable<List<BlockPos>> callback) {
        StructuralEventHandler.onExplosionDetonate(this.level, callback.getReturnValue());
    }
}
