package com.loadbearing.mixin.client;

import com.loadbearing.client.ReinforcementOverlay;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric API has no level render event on this version, so the overlay is submitted at the end of
 * entity submission, which runs inside the main pass with the frame's collector and render state.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "submitEntities", at = @At("TAIL"))
    private void loadbearing$submitReinforcementOverlay(PoseStack poseStack,
            LevelRenderState levelRenderState, SubmitNodeCollector collector,
            CallbackInfo callback) {
        ReinforcementOverlay.submit(poseStack, levelRenderState, collector);
    }
}
