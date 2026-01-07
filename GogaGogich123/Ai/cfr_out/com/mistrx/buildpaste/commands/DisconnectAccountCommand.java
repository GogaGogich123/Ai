/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class DisconnectAccountCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"disconnnectaccount").executes(source -> DisconnectAccountCommand.disconnect((CommandSourceStack)source.getSource(), true)));
    }

    private static Integer disconnect(CommandSourceStack source, boolean deleteall) {
        String result = null;
        try {
            result = Firebase.disconnectAccount(source.getPlayerOrException().getStringUUID(), deleteall);
        }
        catch (CommandSyntaxException e) {
            source.sendFailure((Component)SimpleComponent.error("command-disconnectaccount-exception", "Error disconnection your account", "Player Exception or error with disconnectAccount method"));
            e.printStackTrace();
            return 0;
        }
        String finalResult = result;
        source.sendSuccess(() -> Component.translatable((String)"commands.disconnect.success", (Object[])new Object[]{finalResult}), true);
        return 1;
    }
}

