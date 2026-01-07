/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.chat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mistrx.buildpaste.player.PlayerData;
import com.mistrx.buildpaste.player.PlayerDataManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

public class CommonChatMessages {
    private static JsonObject errorMessagesJson;

    public static String[] getErrorMessage(String errorCode) {
        JsonElement elem;
        if (errorMessagesJson == null) {
            try (InputStream in = BuildPasteMod.class.getResourceAsStream("/assets/buildpaste/errors/error_messages.json");){
                if (in == null) {
                    throw new IOException("Resource not found: error_messages.json");
                }
                errorMessagesJson = JsonParser.parseReader((Reader)new InputStreamReader(in)).getAsJsonObject();
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to load error_messages.json", e);
            }
        }
        if ((elem = errorMessagesJson.get(errorCode)) == null || !elem.isJsonArray()) {
            return new String[]{"Unknown error code: " + errorCode, ""};
        }
        return (String[])new Gson().fromJson(elem, String[].class);
    }

    public static void handleCommonError(Player player, String errorCode) {
        if (errorCode.equals("getbuildid/no-account")) {
            SimpleComponent.TextBuilder buildpaste = SimpleComponent.text("Buildpaste.net", ChatFormatting.GREEN).clickOpenUrl("https://buildpaste.net/category?&player=" + player.getDisplayName().getString()).hoverText("Open buildpaste.net and create an account", ChatFormatting.GREEN);
            player.displayClientMessage((Component)Component.translatable((String)"account.required", (Object[])new Object[]{buildpaste}), false);
        } else {
            String[] errorDetails = CommonChatMessages.getErrorMessage(errorCode);
            player.displayClientMessage((Component)SimpleComponent.error(errorCode, errorDetails[0], errorDetails[1]), false);
        }
    }

    public static MutableComponent getIncompatibleBlocksComponent(Player player) {
        PlayerData playerData = PlayerDataManager.getOrCreatePlayerData(player);
        MutableComponent incompatibleBlocksComponent = Component.literal((String)(playerData.getIncompatibleBlocksAmount() != 0 && !playerData.getIncompatibleBlocksExampleBlocksReplacedArray().isEmpty() ? " (" + playerData.getIncompatibleBlocksAmount() + " incompatible blocks replaced with similar ones)" : ""));
        Object exampleBlockString = "";
        int blockAmountInExampleBlocks = 0;
        for (int i = 0; i < playerData.getIncompatibleBlocksExampleBlocksReplacedArray().size(); ++i) {
            String exampleBlock = playerData.getIncompatibleBlocksExampleBlocksReplacedArray().get(i);
            String[] exampleBlockSplit = exampleBlock.split(",");
            if (exampleBlockSplit.length != 3) continue;
            try {
                blockAmountInExampleBlocks += Integer.parseInt(exampleBlockSplit[2]);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
            exampleBlockString = (String)exampleBlockString + String.valueOf(ChatFormatting.WHITE) + exampleBlockSplit[2] + "x " + String.valueOf(ChatFormatting.RED) + exampleBlockSplit[0] + String.valueOf(ChatFormatting.WHITE) + " -> " + String.valueOf(ChatFormatting.GREEN) + exampleBlockSplit[1] + (i != playerData.getIncompatibleBlocksExampleBlocksReplacedArray().size() - 1 ? "\n" : "");
            if (i != playerData.getIncompatibleBlocksExampleBlocksReplacedArray().size() - 1 || blockAmountInExampleBlocks >= playerData.getIncompatibleBlocksAmount()) continue;
            exampleBlockString = (String)exampleBlockString + "\n" + String.valueOf(ChatFormatting.WHITE) + String.valueOf(ChatFormatting.ITALIC) + "And " + (playerData.getIncompatibleBlocksAmount() - blockAmountInExampleBlocks) + " more blocks...";
        }
        Style incompatibleBlocksStyle = Style.EMPTY.withItalic(Boolean.valueOf(true));
        incompatibleBlocksStyle = incompatibleBlocksStyle.withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)exampleBlockString)));
        incompatibleBlocksComponent.setStyle(incompatibleBlocksStyle);
        return incompatibleBlocksComponent;
    }
}

