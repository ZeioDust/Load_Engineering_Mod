package com.loadbearing.client;

import java.util.ArrayList;
import java.util.List;

import com.loadbearing.net.ReinforcementPayload;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;

public final class ReinforcementOverlay {
    private static final int RENDER_RADIUS = 48;

    private static final int BAR_COLOUR = 0xFFE8E4DA;

    private static final float[][] BARS = {{0.28F, 0.40F}, {0.60F, 0.72F}};

    private static final LongSet REINFORCED = new LongOpenHashSet();

    private static final int REBUILD_MOVE = 8;

    private static int dataGeneration;
    private static int meshGeneration = -1;
    private static Quad[] mesh = new Quad[0];

    private static BlockPos meshOrigin;

    private record Quad(float ax, float ay, float az, float bx, float by, float bz,
                        float cx, float cy, float cz, float dx, float dy, float dz) {}

    private ReinforcementOverlay() {}

    public static void accept(ReinforcementPayload payload) {
        synchronized (REINFORCED) {
            if (payload.replace()) {
                int minX = payload.chunkX() << 4;
                int minZ = payload.chunkZ() << 4;
                REINFORCED.removeIf((long packed) ->
                        BlockPos.getX(packed) >= minX && BlockPos.getX(packed) < minX + 16
                                && BlockPos.getZ(packed) >= minZ && BlockPos.getZ(packed) < minZ + 16);
            }
            for (long packed : payload.positions()) {
                REINFORCED.add(packed);
            }
            dataGeneration++;
        }
    }

    public static void clear() {
        synchronized (REINFORCED) {
            REINFORCED.clear();
            dataGeneration++;
        }
        meshOrigin = null;
    }

    public static void submit(LevelRenderState levelRenderState,
            SubmitNodeCollector collector) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        rebuildIfStale();
        if (mesh.length == 0) {
            return;
        }

        Vec3 camera = levelRenderState.cameraRenderState.pos;
        PoseStack poseStack = new PoseStack();
        Quad[] quads = mesh;

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, buffer) -> {
            for (Quad quad : quads) {
                emit(buffer, pose, quad);
            }
        });
        poseStack.popPose();
    }

    private static void rebuildIfStale() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        BlockPos origin = minecraft.player.blockPosition();

        int generation;
        synchronized (REINFORCED) {
            generation = dataGeneration;
        }

        boolean moved = meshOrigin == null
                || meshOrigin.distSqr(origin) > (double) REBUILD_MOVE * REBUILD_MOVE;
        if (generation == meshGeneration && !moved) {
            return;
        }
        List<Quad> built = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        synchronized (REINFORCED) {
            REINFORCED.removeIf((long packed) -> !minecraft.level.getChunkSource()
                    .hasChunk(BlockPos.getX(packed) >> 4, BlockPos.getZ(packed) >> 4));

            for (long packed : REINFORCED) {
                int x = BlockPos.getX(packed);
                int y = BlockPos.getY(packed);
                int z = BlockPos.getZ(packed);
                if (Math.abs(x - origin.getX()) > RENDER_RADIUS
                        || Math.abs(y - origin.getY()) > RENDER_RADIUS
                        || Math.abs(z - origin.getZ()) > RENDER_RADIUS) {
                    continue;
                }

                cursor.set(x, y, z);
                BlockState state = minecraft.level.getBlockState(cursor);
                if (state.isAir()) {
                    continue;
                }

                for (Direction face : Direction.values()) {
                    cursor.set(x + face.getStepX(), y + face.getStepY(), z + face.getStepZ());
                    if (minecraft.level.getBlockState(cursor).isSolidRender()) {
                        continue;
                    }
                    for (float[] bar : BARS) {
                        built.add(strip(x, y, z, face, bar[0], 0.0F, bar[1], 1.0F));
                        built.add(strip(x, y, z, face, 0.0F, bar[0], 1.0F, bar[1]));
                    }
                }
            }
        }

        mesh = built.toArray(new Quad[0]);
        meshGeneration = generation;
        meshOrigin = origin.immutable();
    }

    private static Quad strip(int x, int y, int z, Direction face,
            float u0, float v0, float u1, float v1) {
        float e = 0.02F;
        float x0 = x;
        float y0 = y;
        float z0 = z;
        float x1 = x0 + 1.0F;
        float y1 = y0 + 1.0F;
        float z1 = z0 + 1.0F;

        return switch (face) {
            case DOWN -> new Quad(
                    x0 + u0, y0 - e, z0 + v0, x0 + u0, y0 - e, z0 + v1,
                    x0 + u1, y0 - e, z0 + v1, x0 + u1, y0 - e, z0 + v0);
            case UP -> new Quad(
                    x0 + u0, y1 + e, z0 + v0, x0 + u1, y1 + e, z0 + v0,
                    x0 + u1, y1 + e, z0 + v1, x0 + u0, y1 + e, z0 + v1);
            case NORTH -> new Quad(
                    x0 + u0, y0 + v0, z0 - e, x0 + u1, y0 + v0, z0 - e,
                    x0 + u1, y0 + v1, z0 - e, x0 + u0, y0 + v1, z0 - e);
            case SOUTH -> new Quad(
                    x0 + u0, y0 + v0, z1 + e, x0 + u0, y0 + v1, z1 + e,
                    x0 + u1, y0 + v1, z1 + e, x0 + u1, y0 + v0, z1 + e);
            case WEST -> new Quad(
                    x0 - e, y0 + v0, z0 + u0, x0 - e, y0 + v1, z0 + u0,
                    x0 - e, y0 + v1, z0 + u1, x0 - e, y0 + v0, z0 + u1);
            case EAST -> new Quad(
                    x1 + e, y0 + v0, z0 + u0, x1 + e, y0 + v0, z0 + u1,
                    x1 + e, y0 + v1, z0 + u1, x1 + e, y0 + v1, z0 + u0);
        };
    }

    private static void emit(VertexConsumer buffer, PoseStack.Pose pose, Quad q) {
        buffer.addVertex(pose, q.ax(), q.ay(), q.az()).setColor(BAR_COLOUR);
        buffer.addVertex(pose, q.bx(), q.by(), q.bz()).setColor(BAR_COLOUR);
        buffer.addVertex(pose, q.cx(), q.cy(), q.cz()).setColor(BAR_COLOUR);
        buffer.addVertex(pose, q.dx(), q.dy(), q.dz()).setColor(BAR_COLOUR);
    }
}
