/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.items.build_placer;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.chat.CommonChatMessages;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.rendering.RenderHandler;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class BuildPlacerHandler {
    public static String status = "";
    public static boolean currentDoRandomRotation = false;
    public static boolean currentDoPlaceOneBlockBelow = false;
    public static boolean currentDoPlaceAir = false;
    public static boolean currentDoSendSuccessMessage = false;
    public static String[] directionsArray = new String[]{"north", "east", "south", "west"};
    public static int currentDirectionIndex = 0;

    public static void handleLeftClick(final Player player) {
        if (Objects.equals(status, "selected")) {
            final String boundBuildId = BuildPlacerHandler.getTag(player, "buildpaste.bound-build-id");
            BuildPasteMod.LOGGER.info("BoundBuildID: " + boundBuildId);
            new Timer().schedule(new TimerTask(){

                @Override
                public void run() {
                    String buildId;
                    status = "loading";
                    if (boundBuildId == null) {
                        try {
                            buildId = Firebase.getSelectedBuildingID(player);
                        }
                        catch (Exception e) {
                            if (e.getMessage().equals("getbuildid/no-account")) {
                                status = "not-connected";
                            }
                            CommonChatMessages.handleCommonError(player, e.getMessage());
                            throw new RuntimeException(e);
                        }
                    } else {
                        buildId = boundBuildId;
                    }
                    try {
                        Firebase.getBuildData(buildId);
                    }
                    catch (Exception e) {
                        CommonChatMessages.handleCommonError(player, e.getMessage());
                        return;
                    }
                    status = "build-loaded";
                    RenderHandler.prepareRenderBlocks(player);
                }
            }, 1L);
        } else if (Objects.equals(status, "build-loaded")) {
            Minecraft.getInstance().player.connection.sendCommand("_pastebuildplacer");
        }
    }

    public static void handleRightClick(Player player) {
        if (Objects.equals(status, "selected")) {
            BuildPasteMod.LOGGER.info("opening on right click");
            Minecraft.getInstance().player.connection.sendCommand("_openbuildplacermenu");
        } else if (Objects.equals(status, "build-loaded")) {
            String currentDirection = directionsArray[currentDirectionIndex];
            System.out.println("Current direction: " + currentDirection);
            currentDirectionIndex = (currentDirectionIndex + 1) % directionsArray.length;
            RenderHandler.renderDirection = directionsArray[currentDirectionIndex];
        }
    }

    public static void loadBuildPlacerVariables(Player player) {
        BuildPlacerHandler.setCurrentDoRandomRotationWithString(player);
        BuildPlacerHandler.setCurrentDoPlaceOneBlockBelow(player);
        BuildPlacerHandler.setCurrentDoPlaceAir(player);
        BuildPlacerHandler.setCurrentDoSendSuccessMessage(player);
    }

    public static void setCurrentDoRandomRotationWithString(Player player) {
        String randomRotationTagValue = BuildPlacerHandler.getTag(player, "buildpaste.do-random-rotate");
        currentDoRandomRotation = Objects.equals(randomRotationTagValue, "true");
    }

    public static void setCurrentDoPlaceOneBlockBelow(Player player) {
        String tagValue = BuildPlacerHandler.getTag(player, "buildpaste.do-place-one-block-below");
        currentDoPlaceOneBlockBelow = Objects.equals(tagValue, "true");
    }

    public static void setCurrentDoPlaceAir(Player player) {
        String tagValue = BuildPlacerHandler.getTag(player, "buildpaste.do-place-air");
        currentDoPlaceAir = Objects.equals(tagValue, "true");
    }

    public static void setCurrentDoSendSuccessMessage(Player player) {
        String tagValue = BuildPlacerHandler.getTag(player, "buildpaste.do-send-success-message");
        currentDoSendSuccessMessage = Objects.equals(tagValue, "true");
    }

    public static void setTag(Player player, String tag, String value) {
        CompoundTag compoundTag = new CompoundTag();
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.getItem() != BuildPasteMod.BUILD_PLACER.get()) {
            player.displayClientMessage((Component)Component.translatable((String)"commands.error", (Object[])new Object[]{"BuildPlacerHandler/No-BuildPlacer-in-hand"}), false);
            return;
        }
        if (itemStack.get(DataComponents.CUSTOM_DATA) != null) {
            compoundTag = ((CustomData)itemStack.get(DataComponents.CUSTOM_DATA)).copyTag();
        }
        if (compoundTag.contains(tag)) {
            if (Objects.equals(value, "true")) {
                compoundTag.putString(tag, "false");
            } else {
                compoundTag.remove(tag);
            }
        } else {
            compoundTag.putString(tag, value);
        }
        CustomData.set((DataComponentType)DataComponents.CUSTOM_DATA, (ItemStack)itemStack, (CompoundTag)compoundTag);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
    }

    public static String getTag(Player player, String tag) {
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(BuildPasteMod.BUILD_PLACER.get()) || player.getItemInHand(InteractionHand.MAIN_HAND).get(DataComponents.CUSTOM_DATA) == null) {
            return null;
        }
        CompoundTag compoundTag = ((CustomData)player.getItemInHand(InteractionHand.MAIN_HAND).get(DataComponents.CUSTOM_DATA)).copyTag();
        if (!compoundTag.contains(tag)) {
            return null;
        }
        return (String)compoundTag.getString(tag).get();
    }
}

