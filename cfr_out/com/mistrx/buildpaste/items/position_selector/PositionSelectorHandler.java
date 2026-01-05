/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.items.position_selector;

import com.mistrx.buildpaste.chat.SimpleComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;

public class PositionSelectorHandler {
    public static Vector3d pos1;
    public static Vector3d pos2;
    public static boolean isUploading;
    public static int uploadSize;

    public static void sendPositionSelectedConfirmationMessage(Player player, Vector3d pos, WHAT_POSITON whatPosition) {
        String positionString = whatPosition == WHAT_POSITON.POS1 ? "1" : "2";
        SimpleComponent.TextBuilder position = SimpleComponent.text("(" + Math.round(pos.x) + ", " + Math.round(pos.y) + ", " + Math.round(pos.z) + ")", ChatFormatting.GREEN).clickRunCommand("/tp " + pos.x + " " + pos.y + " " + pos.z).hoverText("Teleport to Position " + positionString, ChatFormatting.GREEN);
        player.displayClientMessage((Component)Component.translatable((String)("item.buildpaste.position_selector.pos" + positionString), (Object[])new Object[]{position}), false);
    }

    static {
        isUploading = false;
        uploadSize = 0;
    }

    public static enum WHAT_POSITON {
        POS1,
        POS2;

    }
}

