/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"btest").requires(source -> source.hasPermission(4))).executes(source -> TestCommand.test((CommandSourceStack)source.getSource())));
    }

    public static Integer test(CommandSourceStack source) {
        BuildPasteMod.LOGGER.info("TEST COMMAND - BEGIN");
        ServerPlayer player = source.getPlayer();
        assert (player != null);
        source.sendSuccess(() -> Component.translatable((String)"commands.error", (Object[])new Object[]{"TEST"}), true);
        return 1;
    }
}

