/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.rendering;

import com.mistrx.buildpaste.items.position_selector.PositionSelectorHandler;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3d;

public class Render_OLD {
    public static boolean requestedRefresh = false;
    private static BufferBuilder buffer;
    private static String latestDirection;
    private static BlockPos latestStartPosition;
    private static Vector3d latestPos1;
    private static Vector3d latestPos2;
    private static VertexConsumer vc;

    public static void renderBlocks(RenderLevelStageEvent event, int o) {
    }

    public static void renderSelectionOutline(RenderLevelStageEvent event) {
        Vector3d pos1 = PositionSelectorHandler.pos1;
        Vector3d pos2 = PositionSelectorHandler.pos2;
        if (pos1 == null || pos2 == null) {
            return;
        }
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        PoseStack ms = event.getPoseStack();
        ms.pushPose();
        ms.mulPose((Matrix4fc)event.getModelViewMatrix());
        ms.translate(cam.reverse());
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        vc = buffers.getBuffer(RenderType.lines());
        Vector3d low = new Vector3d(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y), Math.min(pos1.z, pos2.z));
        Vector3d high = new Vector3d(Math.max(pos1.x, pos2.x), Math.max(pos1.y, pos2.y), Math.max(pos1.z, pos2.z));
        float x = (float)low.x;
        float y = (float)low.y;
        float z = (float)low.z;
        float dx = (float)(high.x - low.x + 1.0);
        float dy = (float)(high.y - low.y + 1.0);
        float dz = (float)(high.z - low.z + 1.0);
        int argb = Blocks.EMERALD_BLOCK.defaultMapColor().calculateARGBColor(MapColor.Brightness.HIGH);
        float a = (float)(argb >> 24 & 0xFF) / 255.0f;
        float r = (float)(argb >> 16 & 0xFF) / 255.0f;
        float g = (float)(argb >> 8 & 0xFF) / 255.0f;
        float b = (float)(argb & 0xFF) / 255.0f;
        Matrix4f mat = ms.last().pose();
        Render_OLD.drawEdge(mat, x, y + dy, z, x + dx, y + dy, z, r, g, b, a);
        Render_OLD.drawEdge(mat, x + dx, y + dy, z, x + dx, y + dy, z + dz, r, g, b, a);
        Render_OLD.drawEdge(mat, x + dx, y + dy, z + dz, x, y + dy, z + dz, r, g, b, a);
        Render_OLD.drawEdge(mat, x, y + dy, z + dz, x, y + dy, z, r, g, b, a);
        Render_OLD.drawEdge(mat, x, y, z, x + dx, y, z, r, g, b, a);
        Render_OLD.drawEdge(mat, x + dx, y, z, x + dx, y, z + dz, r, g, b, a);
        Render_OLD.drawEdge(mat, x + dx, y, z + dz, x, y, z + dz, r, g, b, a);
        Render_OLD.drawEdge(mat, x, y, z + dz, x, y, z, r, g, b, a);
        Render_OLD.drawEdge(mat, x, y, z, x, y + dy, z, r, g, b, a);
        Render_OLD.drawEdge(mat, x + dx, y, z, x + dx, y + dy, z, r, g, b, a);
        Render_OLD.drawEdge(mat, x + dx, y, z + dz, x + dx, y + dy, z + dz, r, g, b, a);
        Render_OLD.drawEdge(mat, x, y, z + dz, x, y + dy, z + dz, r, g, b, a);
        buffers.endBatch(RenderType.debugFilledBox());
        ms.popPose();
    }

    private static void drawEdge(Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
    }

    static {
        latestDirection = "north";
        latestStartPosition = new BlockPos(0, 0, 0);
    }
}

