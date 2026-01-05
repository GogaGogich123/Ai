/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ConnectAccountsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"connectaccounts").executes(source -> ConnectAccountsCommand.verifyCommand((CommandSourceStack)source.getSource(), ""))).then(Commands.argument((String)"email", (ArgumentType)MessageArgument.message()).executes(source -> ConnectAccountsCommand.verifyCommand((CommandSourceStack)source.getSource(), MessageArgument.getMessage((CommandContext)source, (String)"email").getString()))));
    }

    public static int verifyCommand(CommandSourceStack source, String email) {
        if (!email.contains("@") && !email.isEmpty()) {
            source.sendSuccess(() -> Component.translatable((String)"commands.verify.noemail", (Object[])new Object[]{email}), false);
            return 1;
        }
        if (email.isEmpty()) {
            source.sendSuccess(() -> Component.translatable((String)"commands.verify.help"), false);
        } else {
            ServerPlayer player;
            try {
                player = source.getPlayerOrException();
                PlayerDataManager.getOrCreatePlayerData((Player)player).setMcname(player.getName().getString());
                PlayerDataManager.getOrCreatePlayerData((Player)player).setUuid(player.getStringUUID());
            }
            catch (CommandSyntaxException e) {
                e.printStackTrace();
                source.sendFailure((Component)SimpleComponent.error("commands-verify-commandsyntaxexception", "An error occurred trying to verify.", "mcname and uuid couldn't be assigned"));
                return 0;
            }
            int responseCode = Firebase.connectAccounts((Player)player, email);
            if (responseCode == 200) {
                source.sendSuccess(() -> Component.translatable((String)"commands.verify.verified", (Object[])new Object[]{email}), false);
            } else if (responseCode == 404) {
                SimpleComponent.TextBuilder buildpaste = SimpleComponent.text("Buildpaste.net", ChatFormatting.GREEN).clickOpenUrl("https://buildpaste.net").hoverText("Visit Buildpaste.net", ChatFormatting.GREEN);
                SimpleComponent.TextBuilder profile = SimpleComponent.text("Profile", ChatFormatting.GREEN).clickOpenUrl("https://buildpaste.net/profileedit.html").hoverText("Edit your profile", ChatFormatting.GREEN);
                source.sendFailure((Component)SimpleComponent.translatableError("command-verify-emailnotfound", Component.translatable((String)"commands.verify.emailnotfound", (Object[])new Object[]{email, PlayerDataManager.getOrCreatePlayerData((Player)player).getMcname(), buildpaste, profile})));
            } else {
                source.sendFailure((Component)SimpleComponent.translatableError("command-verify-error", Component.translatable((String)"commands.verify.error"), "Response code by server: " + responseCode));
            }
        }
        return 1;
    }
}

