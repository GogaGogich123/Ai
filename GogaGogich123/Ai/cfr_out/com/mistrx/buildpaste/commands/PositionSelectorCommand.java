/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class PositionSelectorCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"selector").executes(source -> PositionSelectorCommand.getPositionSelector((CommandSourceStack)source.getSource())));
    }

    public static Integer getPositionSelector(CommandSourceStack source) {
        try {
            source.getPlayerOrException().addItem(new ItemStack((ItemLike)BuildPasteMod.POSITION_SELECTOR.get()));
        }
        catch (CommandSyntaxException e) {
            SimpleComponent.TextBuilder help = SimpleComponent.text("this command", ChatFormatting.GREEN).clickSuggestCommand("/give @s buildpaste:position_selector");
            source.sendSuccess(() -> Component.translatable((String)"commands.selector.error", (Object[])new Object[]{help}), false);
            e.printStackTrace();
        }
        return 1;
    }
}

