/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.events.ModClientEvents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.math.Vector3d;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SetFirstPositionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"pos1").requires(source -> source.m_6761_(4))).executes(source -> SetFirstPositionCommand.pasteCommand((CommandSourceStack)source.getSource())));
    }

    public static int pasteCommand(CommandSourceStack source) {
        Vector3d pos = ModClientEvents.pos1 = new Vector3d(Math.floor(source.m_81371_().f_82479_), Math.floor(source.m_81371_().f_82480_), Math.floor(source.m_81371_().f_82481_));
        source.m_81354_((Component)Component.m_237110_((String)"item.buildpaste.position_selecter.pos1", (Object[])new Object[]{"(" + Math.round(pos.f_86214_) + ", " + Math.round(pos.f_86215_) + ", " + Math.round(pos.f_86216_) + ")"}), false);
        return 1;
    }
}

