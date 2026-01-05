/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class _ClearCommand {
    public static void register(CommandDispatcher<CommandSourceStack> p_214421_, CommandBuildContext p_214422_) {
        p_214421_.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"_clearnoresponse").requires(p_136704_ -> p_136704_.m_6761_(2))).executes(p_136721_ -> _ClearCommand.clearInventory((CommandSourceStack)p_136721_.getSource(), Collections.singleton(((CommandSourceStack)p_136721_.getSource()).m_81375_()), p_180029_ -> true, -1))).then(((RequiredArgumentBuilder)Commands.m_82129_((String)"targets", (ArgumentType)EntityArgument.m_91470_()).executes(p_136719_ -> _ClearCommand.clearInventory((CommandSourceStack)p_136719_.getSource(), EntityArgument.m_91477_((CommandContext)p_136719_, (String)"targets"), p_180027_ -> true, -1))).then(((RequiredArgumentBuilder)Commands.m_82129_((String)"item", (ArgumentType)ItemPredicateArgument.m_235353_((CommandBuildContext)p_214422_)).executes(p_136715_ -> _ClearCommand.clearInventory((CommandSourceStack)p_136715_.getSource(), EntityArgument.m_91477_((CommandContext)p_136715_, (String)"targets"), ItemPredicateArgument.m_121040_((CommandContext)p_136715_, (String)"item"), -1))).then(Commands.m_82129_((String)"maxCount", (ArgumentType)IntegerArgumentType.integer((int)0)).executes(p_136702_ -> _ClearCommand.clearInventory((CommandSourceStack)p_136702_.getSource(), EntityArgument.m_91477_((CommandContext)p_136702_, (String)"targets"), ItemPredicateArgument.m_121040_((CommandContext)p_136702_, (String)"item"), IntegerArgumentType.getInteger((CommandContext)p_136702_, (String)"maxCount")))))));
    }

    private static int clearInventory(CommandSourceStack p_136706_, Collection<ServerPlayer> p_136707_, Predicate<ItemStack> p_136708_, int p_136709_) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayer serverplayer : p_136707_) {
            i += serverplayer.m_150109_().m_36022_(p_136708_, p_136709_, (Container)serverplayer.f_36095_.m_39730_());
            serverplayer.f_36096_.m_38946_();
            serverplayer.f_36095_.m_6199_((Container)serverplayer.m_150109_());
        }
        return 1;
    }
}

