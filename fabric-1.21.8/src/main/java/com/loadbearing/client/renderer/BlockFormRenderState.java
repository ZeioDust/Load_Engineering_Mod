package com.loadbearing.client.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This era has a render state but no MovingBlockRenderState, so the block and the position it is
 * seeded from are carried here directly.
 */
public class BlockFormRenderState extends EntityRenderState {
    public BlockState blockState = Blocks.AIR.defaultBlockState();

    public BlockPos blockPos = BlockPos.ZERO;

    public BlockPos randomSeedPos = BlockPos.ZERO;

    public float scale = 1.0F;

    public float spin;

    public float verticalOffset;

    public boolean visible = true;
}
