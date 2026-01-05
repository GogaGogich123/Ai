/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.items.position_selector.PositionSelectorHandler;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;

public class SetSecondPositionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"pos2").executes(source -> SetSecondPositionCommand.pasteCommand((CommandSourceStack)source.getSource())));
    }

    public static int pasteCommand(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Vector3d pos = new Vector3d(Math.floor(source.getPosition().x), Math.floor(source.getPosition().y), Math.floor(source.getPosition().z));
        PlayerDataManager.getOrCreatePlayerData((Player)player).getUploadingDataStore().setPos2(pos);
        PositionSelectorHandler.sendPositionSelectedConfirmationMessage((Player)player, pos, PositionSelectorHandler.WHAT_POSITON.POS2);
        return 1;
    }
}

