/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.util.Functions;
import com.mistrx.buildpaste.util.Variables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class UndoPasteCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"undopaste").requires(source -> source.m_6761_(4))).executes(source -> UndoPasteCommand.undoPaste((CommandSourceStack)source.getSource())));
    }

    public static Integer undoPaste(CommandSourceStack source) {
        if (Variables.lastPos != null) {
            Functions.pasteCurrentBuilding(Variables.lastPos, Variables.lastPasteDirection, "nopastemodifier", true, false);
        } else {
            source.m_81354_((Component)Component.m_237115_((String)"You haven't pasted a build yet"), true);
        }
        return 1;
    }
}

