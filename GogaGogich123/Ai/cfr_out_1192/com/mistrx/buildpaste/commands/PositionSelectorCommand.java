/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.util.Functions;
import com.mistrx.buildpaste.util.Variables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class PositionSelectorCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"selector").requires(source -> source.m_6761_(4))).executes(source -> PositionSelectorCommand.getPositionSelector((CommandSourceStack)source.getSource())));
    }

    public static Integer getPositionSelector(CommandSourceStack source) {
        try {
            Functions.setPlayerVariables((Player)source.m_81375_());
            source.m_81375_().m_20193_().m_7654_().m_129892_().m_82094_().execute("give " + source.m_81375_().m_7755_().getString() + " buildpaste:position_selector", (Object)Variables.player.m_20203_());
        }
        catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 1;
    }
}

