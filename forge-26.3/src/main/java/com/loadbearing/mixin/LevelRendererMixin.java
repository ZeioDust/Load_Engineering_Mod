package com.loadbearing.mixin;

import com.loadbearing.client.ReinforcementOverlay;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Submits the reinforcement overlay at the end of the level's feature submission, which is the
 * same point NeoForge posts SubmitCustomGeometryEvent from. This loader has no such event, and no
 * other hook hands a mod a SubmitNodeCollector for the level.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "submitFeatures", at = @At("TAIL"))
    private void loadbearing$submitReinforcementOverlay(LevelRenderState levelRenderState,
            SubmitNodeCollector submitNodeCollector, boolean renderOutline, CallbackInfo callback) {
        ReinforcementOverlay.submit(levelRenderState, submitNodeCollector);
    }
}
