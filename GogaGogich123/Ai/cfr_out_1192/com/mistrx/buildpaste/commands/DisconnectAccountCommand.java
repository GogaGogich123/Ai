/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.firebase.Firebase;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class DisconnectAccountCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"disconnnectaccount").requires(source -> source.m_6761_(4))).executes(source -> DisconnectAccountCommand.disconnect((CommandSourceStack)source.getSource(), true)));
    }

    private static Integer disconnect(CommandSourceStack source, Boolean deleteall) {
        String result = null;
        try {
            result = Firebase.disconnectAccount(source.m_81375_().m_20149_(), deleteall);
        }
        catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        source.m_81354_((Component)Component.m_237110_((String)"commands.disconnect.success", (Object[])new Object[]{result}), true);
        return 1;
    }
}

