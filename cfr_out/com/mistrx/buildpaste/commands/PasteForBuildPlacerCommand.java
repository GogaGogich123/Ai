/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.chat.CommonChatMessages;
import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mistrx.buildpaste.firebase.BuildDataStore;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.items.build_placer.BuildPlacerHandler;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mistrx.buildpaste.rendering.RenderHandler;
import com.mistrx.buildpaste.util.Functions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;

public class PasteForBuildPlacerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"_pastebuildplacer").executes(source -> PasteForBuildPlacerCommand.run((CommandSourceStack)source.getSource())));
    }

    public static int run(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        String pastemodifier = BuildPlacerHandler.currentDoPlaceAir ? "nopastemodifier" : "dontplaceair";
        BuildDataStore buildData = Firebase.getLocallyStoredBuildData(PlayerDataManager.getOrCreatePlayerData((Player)player).getSelectedBuildId());
        try {
            Functions.pasteBuild((Player)player, buildData, new Vector3d((double)RenderHandler.startPreviewPosition.getX(), (double)RenderHandler.startPreviewPosition.getY(), (double)RenderHandler.startPreviewPosition.getZ()), RenderHandler.renderDirection, pastemodifier, false);
        }
        catch (Exception e) {
            CommonChatMessages.handleCommonError((Player)player, e.getMessage());
            return 0;
        }
        if (BuildPlacerHandler.currentDoRandomRotation) {
            RenderHandler.renderDirection = Functions.getStringLookDirection((float)(Math.random() * 360.0));
            BuildPlacerHandler.currentDirectionIndex = Arrays.asList(BuildPlacerHandler.directionsArray).indexOf(RenderHandler.renderDirection);
        }
        if (BuildPlacerHandler.currentDoSendSuccessMessage) {
            int totalBlockCount = (int)(buildData.getSize().x * buildData.getSize().y * buildData.getSize().z);
            String firstPos = Functions.PosToString(PlayerDataManager.getOrCreatePlayerData((Player)player).getLastFirstPos());
            String secondPos = Functions.PosToString(PlayerDataManager.getOrCreatePlayerData((Player)player).getLastSecondPos());
            SimpleComponent.TextBuilder quickActions = SimpleComponent.text("[Quick Actions]", ChatFormatting.GOLD).clickRunCommand("/_openpastemenu " + firstPos + " " + secondPos + " " + PlayerDataManager.getOrCreatePlayerData((Player)player).getSelectedBuildId()).hoverText("Customize blocks, undo and more", ChatFormatting.GOLD);
            MutableComponent incompatibleBlocksComponent = CommonChatMessages.getIncompatibleBlocksComponent((Player)player);
            source.sendSuccess(() -> Component.translatable((String)"commands.paste.success", (Object[])new Object[]{String.valueOf(totalBlockCount), incompatibleBlocksComponent, quickActions}), true);
        }
        if (RenderHandler.isUsingPreviewCommand) {
            RenderHandler.stopRendering();
        }
        return 1;
    }
}

