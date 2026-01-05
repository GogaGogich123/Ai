/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class BuildPlacerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"buildplacer").executes(source -> BuildPlacerCommand.getPositionSelector((CommandSourceStack)source.getSource())));
    }

    public static Integer getPositionSelector(CommandSourceStack source) {
        try {
            source.getPlayerOrException().addItem(new ItemStack((ItemLike)BuildPasteMod.BUILD_PLACER.get()));
        }
        catch (CommandSyntaxException e) {
            source.sendFailure((Component)SimpleComponent.error("command-buildplacer-commandsyntaxexception", "Failed to obtain the Build Placer item. You can also find the item in the creative menu"));
        }
        return 1;
    }
}

