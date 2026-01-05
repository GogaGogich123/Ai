/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.rendering.RenderHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class _RemoveRenderCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"_removerender").executes(source -> _RemoveRenderCommand.run((CommandSourceStack)source.getSource())));
    }

    public static int run(CommandSourceStack source) throws CommandSyntaxException {
        RenderHandler.stopRendering();
        return 1;
    }
}

