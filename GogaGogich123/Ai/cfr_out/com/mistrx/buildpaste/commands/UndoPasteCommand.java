/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mistrx.buildpaste.util.Functions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class UndoPasteCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"undopaste").executes(source -> UndoPasteCommand.undoPaste((CommandSourceStack)source.getSource())));
    }

    public static Integer undoPaste(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        }
        catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        if (PlayerDataManager.getOrCreatePlayerData((Player)player).getUndoBuildData().getLastPastePlayerPos() != null) {
            BuildPasteMod.LOGGER.info(PlayerDataManager.getOrCreatePlayerData((Player)player).getUndoBuildData().getLastBlockIDs().toString());
            try {
                Functions.pasteBuild((Player)player, PlayerDataManager.getOrCreatePlayerData((Player)player).getUndoBuildData().toBuildDataStore(), PlayerDataManager.getOrCreatePlayerData((Player)player).getUndoBuildData().getLastPastePlayerPos(), PlayerDataManager.getOrCreatePlayerData((Player)player).getUndoBuildData().getLastDirection(), "nopastemodifier", false);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            source.sendSuccess(() -> Component.literal((String)"You haven't pasted a build yet"), true);
        }
        return 1;
    }
}

