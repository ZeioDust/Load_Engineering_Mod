package com.loadbearing.client.renderer;

import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class BlockFormRenderState extends EntityRenderState {
    public final MovingBlockRenderState block = new MovingBlockRenderState();

    public float scale = 1.0F;

    public float spin;

    public float verticalOffset;

    public boolean visible = true;
}
