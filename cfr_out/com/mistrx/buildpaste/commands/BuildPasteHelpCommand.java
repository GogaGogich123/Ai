/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class BuildPasteHelpCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"buildpaste").executes(source -> BuildPasteHelpCommand.help((CommandSourceStack)source.getSource())));
    }

    public static int help(CommandSourceStack source) {
        String url = "https://buildpaste.net/category.html";
        SimpleComponent.TextBuilder component = SimpleComponent.text("Buildpaste.net", ChatFormatting.GREEN).clickOpenUrl(url).hoverText("Visit Buildpaste.net", ChatFormatting.GREEN);
        String message1 = " BuildPaste is a mod, that allows you to\u00a76 Copy and Paste\u00a7F Minecraft Builds! Go to";
        String message2 = "\u00a7Fto copy a build and type \u00a76/paste\u00a7F into the chat to paste it";
        source.sendSuccess(() -> Component.translatable((String)"commands.help", (Object[])new Object[]{message1, component, message2}), true);
        return 1;
    }
}

