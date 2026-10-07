package com.loadbearing.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockFormRenderer<T extends Entity> extends EntityRenderer<T, BlockFormRenderState> {
    protected BlockFormRenderer(EntityRendererProvider.Context context, float shadowRadius) {
        super(context);
        this.shadowRadius = shadowRadius;
    }

    protected abstract BlockState blockFor(T entity);

    protected float scaleFor(T entity) {
        return 1.0F;
    }

    protected float spinFor(T entity, float partialTicks) {
        return 0.0F;
    }

    protected float verticalOffsetFor(T entity) {
        return 0.0F;
    }

    @Override
    public BlockFormRenderState createRenderState() {
        return new BlockFormRenderState();
    }

    @Override
    public void extractRenderState(T entity, BlockFormRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        BlockState blockState = blockFor(entity);
        state.visible = !blockState.isAir() && blockState.getRenderShape() == RenderShape.MODEL;
        state.scale = scaleFor(entity);
        state.spin = spinFor(entity, partialTicks);
        state.verticalOffset = verticalOffsetFor(entity);

        BlockPos pos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        state.block.randomSeedPos = entity.blockPosition();
        state.block.blockPos = pos;
        state.block.blockState = blockState;
        if (entity.level() instanceof ClientLevel clientLevel) {
            state.block.biome = clientLevel.getBiome(pos);
            state.block.cardinalLighting = clientLevel.cardinalLighting();
            state.block.lightEngine = clientLevel.getLightEngine();
        }
    }

    @Override
    public void submit(BlockFormRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.visible) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, state.verticalOffset, 0.0F);
        if (state.scale != 1.0F) {
            poseStack.scale(state.scale, state.scale, state.scale);
        }
        if (state.spin != 0.0F) {
            poseStack.rotateAround(Axis.YP.rotationDegrees(state.spin), 0.0F, 0.5F, 0.0F);
        }
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        collector.submitMovingBlock(poseStack, state.block, state.outlineColor);
        poseStack.popPose();

        super.submit(state, poseStack, collector, camera);
    }
}
