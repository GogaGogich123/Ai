/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class RemovePosCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"removepos").executes(source -> RemovePosCommand.removePositions((CommandSourceStack)source.getSource())));
    }

    private static int removePositions(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerDataManager.getOrCreatePlayerData((Player)player).getUploadingDataStore().setPos1(null);
        PlayerDataManager.getOrCreatePlayerData((Player)player).getUploadingDataStore().setPos2(null);
        source.sendSuccess(() -> Component.translatable((String)"commands.removepos.success"), true);
        return 1;
    }
}

