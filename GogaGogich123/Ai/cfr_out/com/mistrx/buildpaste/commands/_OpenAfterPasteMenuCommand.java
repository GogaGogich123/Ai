/*
 * Decompiled with CFR.
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

public class _OpenAfterPasteMenuCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"_openpastemenu").then(Commands.argument((String)"pos1", (ArgumentType)BlockPosArgument.blockPos()).then(Commands.argument((String)"pos2", (ArgumentType)BlockPosArgument.blockPos()).then(Commands.argument((String)"id", (ArgumentType)StringArgumentType.string()).executes(source -> _OpenAfterPasteMenuCommand.openMenu((CommandSourceStack)source.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext)source, (String)"pos1"), BlockPosArgument.getLoadedBlockPos((CommandContext)source, (String)"pos2"), StringArgumentType.getString((CommandContext)source, (String)"id")))))));
    }

    public static int openMenu(CommandSourceStack source, BlockPos pos1, BlockPos pos2, String id) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PasteMenuHandler.pos1 = pos1;
        PasteMenuHandler.pos2 = pos2;
        PasteMenuHandler.id = id;
        PasteMenuHandler.SetMainMenu("paste-menu", (Player)player);
        return 1;
    }
}

