/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CopyUploadedBuildCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"_copybuildid").then(Commands.argument((String)"id", (ArgumentType)StringArgumentType.string()).executes(source -> CopyUploadedBuildCommand.copyBuildId((CommandSourceStack)source.getSource(), StringArgumentType.getString((CommandContext)source, (String)"id")))));
    }

    private static Integer copyBuildId(CommandSourceStack source, String id) {
        int response = 1;
        try {
            String uuid = source.getPlayerOrException().getStringUUID();
            response = Firebase.setSelectedBuildingID(uuid, id);
        }
        catch (CommandSyntaxException e) {
            e.printStackTrace();
            source.sendFailure((Component)SimpleComponent.error("command-copybuild-playerexception", "Error copying build id; couldn't get player id"));
            return 0;
        }
        if (response == 1) {
            source.sendSuccess(() -> Component.translatable((String)"commands.copyuploadedbuildid.success"), true);
            return 1;
        }
        SimpleComponent.TextBuilder buildpaste = SimpleComponent.text("Buildpaste", ChatFormatting.GREEN).clickOpenUrl("https://buildpaste.net").hoverText("Open buildpaste.net and create an account", ChatFormatting.GREEN);
        SimpleComponent.TextBuilder component = SimpleComponent.text("Copy to clipboard", ChatFormatting.AQUA).clickCopyToClipboard("/paste" + id).hoverText("Copy build to clipboard", ChatFormatting.AQUA);
        source.sendSuccess(() -> Component.translatable((String)"commands.copyuploadedbuildid.error", (Object[])new Object[]{buildpaste, component}), true);
        return 1;
    }
}

