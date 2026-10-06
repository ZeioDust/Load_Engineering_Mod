package com.loadbearing.client.renderer;

import com.loadbearing.entity.DustCloudEntity;
import com.loadbearing.entity.FallingDebrisEntity;
import com.loadbearing.registry.LBEntities;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.EntityRenderersEvent;

public final class LBEntityRenderers {
    private LBEntityRenderers() {}

    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(LBEntities.FALLING_DEBRIS.get(), DebrisRenderer::new);
        event.registerEntityRenderer(LBEntities.DUST_CLOUD.get(), DustCloudRenderer::new);
    }

    public static class DebrisRenderer extends BlockFormRenderer<FallingDebrisEntity> {
        public DebrisRenderer(EntityRendererProvider.Context context) {
            super(context, 0.5F);
        }

        @Override
        protected BlockState blockFor(FallingDebrisEntity entity) {
            return entity.getBlockState();
        }

        @Override
        protected float spinFor(FallingDebrisEntity entity, float partialTicks) {
            float rate = entity.isShattered() ? 6.0F : 2.0F;
            return (entity.tumbleTicks() + partialTicks) * rate % 360.0F;
        }
    }

    public static class DustCloudRenderer extends BlockFormRenderer<DustCloudEntity> {
        public DustCloudRenderer(EntityRendererProvider.Context context) {
            super(context, 0.0F);
        }

        @Override
        protected BlockState blockFor(DustCloudEntity entity) {
            return Blocks.AIR.defaultBlockState();
        }
    }
}
