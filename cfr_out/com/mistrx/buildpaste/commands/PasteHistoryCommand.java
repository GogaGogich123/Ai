/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.history.PasteHistoryHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class PasteHistoryCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"buildhistory").executes(source -> PasteHistoryCommand.run((CommandSourceStack)source.getSource())));
    }

    private static int run(CommandSourceStack source) {
        List<String> lastPastedBuildIDs = Firebase.lastPastedBuilds;
        int maxSize = 20;
        int size = Math.min(lastPastedBuildIDs.size(), maxSize);
        if (size == 0) {
            source.sendSuccess(() -> Component.translatable((String)"commands.pastehistory.empty"), true);
            return 0;
        }
        MutableComponent component = Component.literal((String)"");
        for (int i = 0; i < size; ++i) {
            String id = lastPastedBuildIDs.get(lastPastedBuildIDs.size() - 1 - i);
            Object name = "";
            if (PasteHistoryHandler.buildNames.containsKey(id)) {
                name = PasteHistoryHandler.buildNames.get(id);
            } else {
                name = Firebase.getBuildName(id);
                if (name == null || ((String)name).isEmpty()) {
                    name = "Unnamed (" + id.substring(0, 8) + "...)";
                }
                PasteHistoryHandler.buildNames.put(id, (String)name);
            }
            component.append("\n");
            Style componentStyle = Style.EMPTY;
            componentStyle = componentStyle.applyFormat(ChatFormatting.AQUA);
            componentStyle = componentStyle.withClickEvent((ClickEvent)new ClickEvent.RunCommand("/_copybuildid " + id));
            componentStyle = componentStyle.withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)("Copy " + (String)name))));
            component.append((Component)Component.literal((String)(i + 1 + ". ")).withStyle(ChatFormatting.WHITE).append((Component)Component.literal((String)name).withStyle(componentStyle)));
        }
        source.sendSuccess(() -> Component.translatable((String)"commands.pastehistory.success", (Object[])new Object[]{component}), true);
        return 1;
    }
}

