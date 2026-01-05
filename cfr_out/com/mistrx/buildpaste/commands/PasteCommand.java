/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.chat.CommonChatMessages;
import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mistrx.buildpaste.firebase.BuildDataStore;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.pasting.PasteHandler;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mistrx.buildpaste.player.PlayerFunctions;
import com.mistrx.buildpaste.util.Functions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;

public final class PasteCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"paste").executes(source -> PasteCommand.pasteCommand((CommandSourceStack)source.getSource(), null, null))).then(Commands.argument((String)"first argument", (ArgumentType)StringArgumentType.string()).executes(source -> PasteCommand.pasteCommand((CommandSourceStack)source.getSource(), StringArgumentType.getString((CommandContext)source, (String)"first argument"), null)))).then(Commands.argument((String)"first argument", (ArgumentType)StringArgumentType.string()).then(Commands.argument((String)"second argument", (ArgumentType)StringArgumentType.string()).executes(source -> PasteCommand.pasteCommand((CommandSourceStack)source.getSource(), StringArgumentType.getString((CommandContext)source, (String)"first argument"), StringArgumentType.getString((CommandContext)source, (String)"second argument"))))));
    }

    public static int pasteCommand(CommandSourceStack source, String firstarg, String secondarg) throws CommandSyntaxException {
        BuildDataStore buildData;
        String direction;
        ServerPlayer player;
        String[] args = new String[]{firstarg, secondarg};
        BuildPasteMod.LOGGER.info("Paste Command executed");
        try {
            player = source.getPlayerOrException();
            direction = Functions.getPlayerStringLookDirection((Player)player);
            if (PlayerFunctions.hasInvalidUUID((Player)player).booleanValue()) {
                source.sendSuccess(() -> Component.translatable((String)"util.doesnt-have-valid-uuid"), true);
            }
        }
        catch (CommandSyntaxException e) {
            BuildPasteMod.LOGGER.info("Error in try catch at pasteCommand");
            e.printStackTrace();
            source.sendFailure((Component)SimpleComponent.error("command-paste-exception", "Error getting player object. Try restarting the game or seek help."));
            return 0;
        }
        String pasteModifier = PasteHandler.getPasteModifierFromArgs(args);
        String buildId = PasteHandler.getBuildIdFromArgs(args);
        if (buildId == null) {
            try {
                buildId = Firebase.getSelectedBuildingID((Player)player);
            }
            catch (Exception e) {
                CommonChatMessages.handleCommonError((Player)player, e.getMessage());
                return 0;
            }
        }
        try {
            buildData = Firebase.getBuildData(buildId);
        }
        catch (Exception e) {
            CommonChatMessages.handleCommonError((Player)player, e.getMessage());
            return 0;
        }
        try {
            Functions.pasteBuild((Player)player, buildData, new Vector3d(Math.floor(source.getPosition().x), Math.floor(source.getPosition().y), Math.floor(source.getPosition().z)), direction, pasteModifier, false);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        int totalBlockCount = (int)(buildData.getSize().x * buildData.getSize().y * buildData.getSize().z);
        String firstPos = Functions.PosToString(PlayerDataManager.getOrCreatePlayerData((Player)player).getLastFirstPos());
        String secondPos = Functions.PosToString(PlayerDataManager.getOrCreatePlayerData((Player)player).getLastSecondPos());
        SimpleComponent.TextBuilder quickActions = SimpleComponent.text("[Quick Actions]", ChatFormatting.GOLD).clickRunCommand("/_openpastemenu " + firstPos + " " + secondPos + " " + PlayerDataManager.getOrCreatePlayerData((Player)player).getSelectedBuildId()).hoverText("Customize blocks, undo and more", ChatFormatting.GOLD);
        MutableComponent incompatibleBlocksComponent = CommonChatMessages.getIncompatibleBlocksComponent((Player)player);
        source.sendSuccess(() -> Component.translatable((String)"commands.paste.success", (Object[])new Object[]{String.valueOf(totalBlockCount), incompatibleBlocksComponent, quickActions}), true);
        return 1;
    }
}

