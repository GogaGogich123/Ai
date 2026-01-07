/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.rendering;

import com.mistrx.buildpaste.firebase.BuildDataStore;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mistrx.buildpaste.rendering.RenderBlock;
import com.mistrx.buildpaste.rendering.RenderHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Objects;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public class Render {
    public static boolean requestedRefresh = false;
    private static String latestDirection = "north";
    private static BlockPos latestStartPosition = new BlockPos(0, 0, 0);

    public static void renderSelectionOutline(RenderLevelStageEvent event, Player player) {
        Vector3d pos1 = PlayerDataManager.getOrCreatePlayerData(player).getUploadingDataStore().getPos1();
        Vector3d pos2 = PlayerDataManager.getOrCreatePlayerData(player).getUploadingDataStore().getPos2();
        if (pos1 == null || pos2 == null) {
            return;
        }
        BlockPos firstBlockPos = new BlockPos((int)pos1.x, (int)pos1.y, (int)pos1.z);
        BlockPos secondBlockPos = new BlockPos((int)pos2.x, (int)pos2.y, (int)pos2.z);
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return;
        }
        Vec3 vec3 = camera.getPosition().reverse();
        AABB aabb = AABB.encapsulatingFullBlocks((BlockPos)firstBlockPos, (BlockPos)secondBlockPos).move(vec3);
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix4f = poseStack.last().pose();
        int argb = Blocks.EMERALD_BLOCK.defaultMapColor().calculateARGBColor(MapColor.Brightness.HIGH);
        float alpha = (float)(argb >> 24 & 0xFF) / 255.0f;
        alpha = 1.0f;
        float red = (float)(argb >> 16 & 0xFF) / 255.0f;
        float green = (float)(argb >> 8 & 0xFF) / 255.0f;
        float blue = (float)(argb & 0xFF) / 255.0f;
        Render.renderBlockOutline(vc, matrix4f, aabb, red, green, blue, alpha);
        DebugRenderer.renderFilledBox((PoseStack)poseStack, (MultiBufferSource)bufferSource, (AABB)aabb, (float)red, (float)green, (float)blue, (float)0.2f);
    }

    private static void renderBlockOutline(VertexConsumer vertexConsumer, Matrix4f matrix4f, BlockPos pos, Camera camera, float red, float green, float blue, float alpha) {
        Vec3 vec3 = camera.getPosition().reverse();
        AABB aabb = AABB.encapsulatingFullBlocks((BlockPos)pos, (BlockPos)pos).move(vec3);
        Render.renderBlockOutline(vertexConsumer, matrix4f, aabb, red, green, blue, alpha);
    }

    private static void renderBlockOutline(VertexConsumer vc, Matrix4f matrix4f, AABB aabb, float red, float green, float blue, float alpha) {
        float minX = (float)aabb.minX;
        float minY = (float)aabb.minY;
        float minZ = (float)aabb.minZ;
        float maxX = (float)aabb.maxX;
        float maxY = (float)aabb.maxY;
        float maxZ = (float)aabb.maxZ;
        float normalX = 0.1f;
        float normalY = 0.1f;
        float normalZ = 0.1f;
        vc.addVertex(matrix4f, minX, maxY, minZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, maxX, maxY, minZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, maxX, maxY, minZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, maxX, maxY, maxZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, maxX, maxY, maxZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, minX, maxY, maxZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, minX, maxY, maxZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, minX, maxY, minZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, maxX, minY, minZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, maxX, minY, maxZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, maxX, minY, maxZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, minX, minY, maxZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, minX, minY, maxZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, minX, minY, minZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, minX, minY, minZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, maxX, minY, minZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, maxX, minY, maxZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, maxX, maxY, maxZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, maxX, minY, minZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, maxX, maxY, minZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, minX, minY, maxZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, minX, maxY, maxZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, minX, minY, minZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
        vc.addVertex(matrix4f, minX, maxY, minZ).setNormal(normalX, normalY, normalZ).setColor(red, green, blue, alpha);
    }

    public static void renderBlocks(RenderLevelStageEvent event) {
        int b2;
        int a2;
        int c1;
        int b1;
        int a1;
        Camera camera;
        LocalPlayer player = Minecraft.getInstance().player;
        BuildDataStore buildData = Firebase.getLocallyStoredBuildData(PlayerDataManager.getOrCreatePlayerData((Player)player).getSelectedBuildId());
        if (!latestStartPosition.equals((Object)RenderHandler.startPreviewPosition) || !Objects.equals(latestDirection, RenderHandler.renderDirection)) {
            latestStartPosition = RenderHandler.startPreviewPosition;
            latestDirection = RenderHandler.renderDirection;
            requestedRefresh = true;
        }
        if (!(camera = Minecraft.getInstance().gameRenderer.getMainCamera()).isInitialized()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix4f = poseStack.last().pose();
        Vector3d size = buildData.getSize();
        String direction = RenderHandler.renderDirection;
        BlockPos startPreviewPosition = RenderHandler.startPreviewPosition;
        int sx = (int)size.x;
        int sy = (int)size.y;
        int sz = (int)size.z;
        int ox = startPreviewPosition.getX();
        int oy = startPreviewPosition.getY();
        int oz = startPreviewPosition.getZ();
        boolean special = buildData.getUploadDirection().equals("north") || buildData.getUploadDirection().equals("south");
        int c2 = switch (direction) {
            case "north" -> {
                if (special) {
                    a1 = -1;
                    b1 = 0;
                    c1 = sx;
                    a2 = 0;
                    b2 = -1;
                    yield 0;
                }
                a1 = 0;
                b1 = -1;
                c1 = sz;
                a2 = -1;
                b2 = 0;
                yield 0;
            }
            case "south" -> {
                if (special) {
                    a1 = 1;
                    b1 = 0;
                    c1 = -sx;
                    a2 = 0;
                    b2 = 1;
                    yield 0;
                }
                a1 = 0;
                b1 = 1;
                c1 = -sz;
                a2 = 1;
                b2 = 0;
                yield 0;
            }
            case "east" -> {
                if (special) {
                    a1 = 0;
                    b1 = 1;
                    c1 = 0;
                    a2 = -1;
                    b2 = 0;
                    yield sx;
                }
                a1 = 1;
                b1 = 0;
                c1 = 0;
                a2 = 0;
                b2 = -1;
                yield sz;
            }
            case "west" -> {
                if (special) {
                    a1 = 0;
                    b1 = -1;
                    c1 = 0;
                    a2 = 1;
                    b2 = 0;
                    yield -sx;
                }
                a1 = -1;
                b1 = 0;
                c1 = 0;
                a2 = 0;
                b2 = 1;
                yield -sz;
            }
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        };
        int i = 0;
        for (int x = 0; x < sx; ++x) {
            for (int y = 0; y < sy; ++y) {
                for (int z = 0; z < sz; ++z) {
                    int dx = a1 * x + b1 * z + c1;
                    int dz = a2 * x + b2 * z + c2;
                    BlockPos currentPos = new BlockPos(ox + dx, oy + y, oz + dz);
                    Render.addBlock(currentPos, RenderHandler.renderBlocks.get(i++), vc, matrix4f, camera);
                }
            }
        }
    }

    private static void addBlock(BlockPos pos, RenderBlock renderBlock, VertexConsumer vertexConsumer, Matrix4f matrix4f, Camera camera) {
        if (renderBlock == null) {
            return;
        }
        int argb = renderBlock.getColor();
        float a = (float)(argb >> 24 & 0xFF) / 255.0f;
        float r = (float)(argb >> 16 & 0xFF) / 255.0f;
        float g = (float)(argb >> 8 & 0xFF) / 255.0f;
        float b = (float)(argb & 0xFF) / 255.0f;
        Render.renderBlockOutline(vertexConsumer, matrix4f, pos, camera, r, g, b, a);
    }
}

