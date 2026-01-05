/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.util;

import com.mistrx.buildpaste.chat.SimpleComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

public class CommandChainLengthHandler {
    private static final int DESIRED_MAX_COMMAND_CHAIN_LENGTH = 5000000;

    public static void increaseMaxCommandChainLengthIfNecessary(MinecraftServer server) {
        if (server == null) {
            return;
        }
        int value = ((GameRules.IntegerValue)server.getGameRules().getRule(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH)).get();
        if (value > 5000000) {
            return;
        }
        ((GameRules.IntegerValue)server.getGameRules().getRule(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH)).set(5000000, server);
    }

    public static void verifyMaxCommandChainLength(Player player) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            int value = ((GameRules.IntegerValue)server.getGameRules().getRule(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH)).get();
            if (value >= 5000000) {
                return;
            }
            ((GameRules.IntegerValue)server.getGameRules().getRule(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH)).set(5000000, server);
            player.displayClientMessage((Component)SimpleComponent.text("Some blocks likely didn\u2019t paste. We've automatically increased the maxCommandChainLength to fix this. If it still fails, raise the gamerule manually or contact support."), false);
        } else {
            player.displayClientMessage((Component)SimpleComponent.text("Error finding the server. Please make sure your maxCommandChainLength gamerule is big enough to paste or contact support."), false);
        }
    }
}

