package com.loadbearing.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The pre-render-state form of the renderer, drawing through the block render dispatcher. This
 * variant uses only vanilla API, since this loader adds no render-type helper of its own.
 */
public abstract class BlockFormRenderer<T extends Entity> extends EntityRenderer<T> {
    private final BlockRenderDispatcher dispatcher;

    protected BlockFormRenderer(EntityRendererProvider.Context context, float shadowRadius) {
        super(context);
        this.shadowRadius = shadowRadius;
        this.dispatcher = context.getBlockRenderDispatcher();
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
    public ResourceLocation getTextureLocation(T entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(T entity, float yaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffers, int light) {
        BlockState blockState = blockFor(entity);
        if (blockState.isAir() || blockState.getRenderShape() != RenderShape.MODEL) {
            return;
        }

        Level level = entity.level();
        BlockPos pos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        float scale = scaleFor(entity);
        float spin = spinFor(entity, partialTicks);

        poseStack.pushPose();
        poseStack.translate(0.0F, verticalOffsetFor(entity), 0.0F);
        if (scale != 1.0F) {
            poseStack.scale(scale, scale, scale);
        }
        if (spin != 0.0F) {
            poseStack.rotateAround(Axis.YP.rotationDegrees(spin), 0.0F, 0.5F, 0.0F);
        }
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        long seed = blockState.getSeed(entity.blockPosition());
        this.dispatcher.getModelRenderer().tesselateBlock(
                level, this.dispatcher.getBlockModel(blockState), blockState, pos, poseStack,
                buffers.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(blockState)),
                false, RandomSource.create(), seed, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, buffers, light);
    }
}
