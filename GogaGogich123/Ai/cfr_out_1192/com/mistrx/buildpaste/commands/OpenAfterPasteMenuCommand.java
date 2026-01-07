/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.gui.PasteMenuHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class OpenAfterPasteMenuCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"_openpastemenu").requires(source -> source.m_6761_(4))).then(Commands.m_82129_((String)"pos1", (ArgumentType)BlockPosArgument.m_118239_()).then(Commands.m_82129_((String)"pos2", (ArgumentType)BlockPosArgument.m_118239_()).then(Commands.m_82129_((String)"id", (ArgumentType)StringArgumentType.string()).executes(source -> OpenAfterPasteMenuCommand.openMenu((CommandSourceStack)source.getSource(), BlockPosArgument.m_118242_((CommandContext)source, (String)"pos1"), BlockPosArgument.m_118242_((CommandContext)source, (String)"pos2"), StringArgumentType.getString((CommandContext)source, (String)"id")))))));
    }

    public static int openMenu(CommandSourceStack source, BlockPos pos1, BlockPos pos2, String id) throws CommandSyntaxException {
        ServerPlayer player = source.m_81375_();
        PasteMenuHandler.pos1 = pos1;
        PasteMenuHandler.pos2 = pos2;
        PasteMenuHandler.id = id;
        PasteMenuHandler.SetMainMenu((Player)player);
        return 1;
    }
}

