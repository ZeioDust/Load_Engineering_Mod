package com.loadbearing.mixin;

import com.loadbearing.client.ReinforcementOverlay;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This Forge version exposes no level render event and no frame pass, so the overlay is submitted
 * at the end of block entity rendering, which runs inside the main pass with a live buffer source
 * and the frame's camera. Same geometry, same render type, same camera offset.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "renderBlockEntities", at = @At("TAIL"))
    private void loadbearing$submitReinforcementOverlay(PoseStack poseStack,
            MultiBufferSource.BufferSource buffers, MultiBufferSource.BufferSource crumbling,
            Camera camera, float partialTick, Frustum frustum,
            CallbackInfoReturnable<Boolean> callback) {
        ReinforcementOverlay.submit(poseStack, buffers, camera);
    }
}
