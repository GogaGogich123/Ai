/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.util.Variables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class BuildPasteHelpCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"buildpaste").executes(source -> BuildPasteHelpCommand.help((CommandSourceStack)source.getSource())));
    }

    public static int help(CommandSourceStack source) {
        String url = Variables.url + "/category.html";
        MutableComponent urltextcomponent = Component.m_237113_((String)"Buildpaste.net");
        Style componentStyle = Style.f_131099_;
        componentStyle = componentStyle.m_131157_(ChatFormatting.GREEN);
        componentStyle = componentStyle.m_131142_(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        componentStyle = componentStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Visit Buildpaste.net").m_6270_(Style.f_131099_.m_131157_(ChatFormatting.GREEN))));
        urltextcomponent.m_6270_(componentStyle);
        String message1 = ChatFormatting.AQUA + " BuildPaste is a mod, that allows you to" + ChatFormatting.GOLD + " Copy and Paste" + ChatFormatting.AQUA + " Minecraft Builds! Go to";
        String message2 = ChatFormatting.AQUA + "to copy a build and type " + ChatFormatting.GOLD + "/paste" + ChatFormatting.AQUA + " into the chat to paste it";
        source.m_81354_((Component)Component.m_237110_((String)"commands.help", (Object[])new Object[]{message1, urltextcomponent, message2}), true);
        return 1;
    }
}

