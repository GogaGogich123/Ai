/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.rendering;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.firebase.BuildDataStore;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.items.build_placer.BuildPlacerHandler;
import com.mistrx.buildpaste.pasting.AllBlocksArray;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mistrx.buildpaste.rendering.RenderBlock;
import com.mistrx.buildpaste.util.Functions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;

public class RenderHandler {
    public static BlockPos startPreviewPosition = new BlockPos(0, 0, 0);
    public static String renderDirection = "north";
    private static String previewedBuildID = "";
    public static boolean shouldRenderStructure = false;
    public static boolean hasRaycastHitSomething = false;
    public static boolean isUsingPreviewCommand = false;
    public static List<RenderBlock> renderBlocks = new ArrayList<RenderBlock>();

    public static void prepareRenderBlocks(Player player) {
        BuildPlacerHandler.loadBuildPlacerVariables(player);
        BuildDataStore buildData = Firebase.getLocallyStoredBuildData(PlayerDataManager.getOrCreatePlayerData(player).getSelectedBuildId());
        if (buildData == null) {
            return;
        }
        BuildPasteMod.LOGGER.info("Current do random rotation: " + BuildPlacerHandler.currentDoRandomRotation);
        if (!isUsingPreviewCommand && BuildPlacerHandler.currentDoRandomRotation) {
            BuildPasteMod.LOGGER.info("Random Direction");
            renderDirection = Functions.getStringLookDirection((float)(Math.random() * 360.0));
        } else {
            renderDirection = Functions.getPlayerStringLookDirection(player);
        }
        BuildPasteMod.LOGGER.info("Look Direction: " + renderDirection);
        BuildPlacerHandler.currentDirectionIndex = Arrays.asList(BuildPlacerHandler.directionsArray).indexOf(renderDirection);
        for (int i = 0; i < buildData.getBlockIds().size(); ++i) {
            int id;
            try {
                id = Math.round(Float.parseFloat(buildData.getBlockIds().get(i).toString()));
            }
            catch (Exception e) {
                renderBlocks.add(null);
                continue;
            }
            ResourceLocation resourceLocation = ResourceLocation.parse((String)("minecraft:" + AllBlocksArray.blocksArray[id]));
            Block foundBlock = (Block)BuiltInRegistries.BLOCK.getValue(resourceLocation);
            if (foundBlock == Blocks.AIR || foundBlock.defaultBlockState().getRenderShape() != RenderShape.MODEL) {
                renderBlocks.add(null);
                continue;
            }
            int color = foundBlock.defaultMapColor().calculateARGBColor(MapColor.Brightness.HIGH);
            RenderBlock renderBlock = new RenderBlock(color);
            renderBlocks.add(renderBlock);
        }
        shouldRenderStructure = true;
    }

    public static void stopRendering() {
        isUsingPreviewCommand = false;
        shouldRenderStructure = false;
        renderBlocks.clear();
    }

    public static void calculateStartPreviewPositionWithRaycastPosition(BlockPos rayHit, Player player) {
        Vec3 startPos;
        BuildDataStore buildData = Firebase.getLocallyStoredBuildData(PlayerDataManager.getOrCreatePlayerData(player).getSelectedBuildId());
        if (buildData == null) {
            return;
        }
        if (buildData.getSize() == null) {
            return;
        }
        if (buildData.getUploadDirection().equals("north") || buildData.getUploadDirection().equals("south")) {
            switch (renderDirection) {
                case "east": {
                    startPos = new Vec3((double)rayHit.getX() - buildData.getSize().z / 2.0 + 1.0, (double)rayHit.getY(), (double)rayHit.getZ() - buildData.getSize().x / 2.0);
                    break;
                }
                case "south": {
                    startPos = new Vec3((double)rayHit.getX() + buildData.getSize().x / 2.0 + 1.0, (double)rayHit.getY(), (double)rayHit.getZ() - buildData.getSize().z / 2.0 + 1.0);
                    break;
                }
                case "west": {
                    startPos = new Vec3((double)rayHit.getX() + buildData.getSize().z / 2.0, (double)rayHit.getY(), (double)rayHit.getZ() + buildData.getSize().x / 2.0 + 1.0);
                    break;
                }
                default: {
                    startPos = new Vec3((double)rayHit.getX() - buildData.getSize().x / 2.0, (double)rayHit.getY(), (double)rayHit.getZ() + buildData.getSize().z / 2.0);
                    break;
                }
            }
        } else {
            switch (renderDirection) {
                case "east": {
                    startPos = new Vec3((double)rayHit.getX() - buildData.getSize().x / 2.0 + 1.0, (double)rayHit.getY(), (double)rayHit.getZ() - buildData.getSize().z / 2.0);
                    break;
                }
                case "south": {
                    startPos = new Vec3((double)rayHit.getX() + buildData.getSize().z / 2.0 + 1.0, (double)rayHit.getY(), (double)rayHit.getZ() - buildData.getSize().x / 2.0 + 1.0);
                    break;
                }
                case "west": {
                    startPos = new Vec3((double)rayHit.getX() + buildData.getSize().x / 2.0, (double)rayHit.getY(), (double)rayHit.getZ() + buildData.getSize().z / 2.0 + 1.0);
                    break;
                }
                default: {
                    startPos = new Vec3((double)rayHit.getX() - buildData.getSize().z / 2.0, (double)rayHit.getY(), (double)rayHit.getZ() + buildData.getSize().x / 2.0);
                }
            }
        }
        int yPos = (int)Math.round(startPos.y);
        if (!BuildPlacerHandler.currentDoPlaceOneBlockBelow) {
            ++yPos;
        }
        startPreviewPosition = new BlockPos((int)Math.floor(startPos.x), yPos, (int)Math.floor(startPos.z));
    }
}

