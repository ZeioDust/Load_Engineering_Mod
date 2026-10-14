package com.loadbearing.mixin;

import com.loadbearing.solver.StructuralEventHandler;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stands in for the explosion-detonate event the other loaders provide. In this era the explosion
 * is a single class and getToBlow holds the blocks it is about to remove.
 */
@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow
    private Level level;

    @Shadow
    public abstract java.util.List<net.minecraft.core.BlockPos> getToBlow();

    @Inject(method = "explode", at = @At("RETURN"))
    private void loadbearing$afterExplode(CallbackInfo callback) {
        if (this.level instanceof ServerLevel serverLevel) {
            StructuralEventHandler.onExplosionDetonate(serverLevel, getToBlow());
        }
    }
}
