/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.events.ModClientEvents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class RemovePosCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"removepos").requires(source -> source.m_6761_(4))).executes(source -> RemovePosCommand.removePositions((CommandSourceStack)source.getSource())));
    }

    private static int removePositions(CommandSourceStack source) {
        ModClientEvents.pos1 = null;
        ModClientEvents.pos2 = null;
        source.m_81354_((Component)Component.m_237115_((String)"commands.removepos.success"), true);
        return 1;
    }
}

