/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class _ClearCommand {
    private static final DynamicCommandExceptionType ERROR_SINGLE = new DynamicCommandExceptionType(p_304193_ -> Component.translatableEscape((String)"clear.failed.single", (Object[])new Object[]{p_304193_}));
    private static final DynamicCommandExceptionType ERROR_MULTIPLE = new DynamicCommandExceptionType(p_304192_ -> Component.translatableEscape((String)"clear.failed.multiple", (Object[])new Object[]{p_304192_}));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"_clearnoresponse").requires(p_136704_ -> p_136704_.hasPermission(2))).executes(p_332568_ -> _ClearCommand.clearUnlimited((CommandSourceStack)p_332568_.getSource(), Collections.singleton(((CommandSourceStack)p_332568_.getSource()).getPlayerOrException()), p_180029_ -> true))).then(((RequiredArgumentBuilder)Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).executes(p_332566_ -> _ClearCommand.clearUnlimited((CommandSourceStack)p_332566_.getSource(), EntityArgument.getPlayers((CommandContext)p_332566_, (String)"targets"), p_180027_ -> true))).then(((RequiredArgumentBuilder)Commands.argument((String)"item", (ArgumentType)ItemPredicateArgument.itemPredicate((CommandBuildContext)context)).executes(p_332567_ -> _ClearCommand.clearUnlimited((CommandSourceStack)p_332567_.getSource(), EntityArgument.getPlayers((CommandContext)p_332567_, (String)"targets"), (Predicate<ItemStack>)ItemPredicateArgument.getItemPredicate((CommandContext)p_332567_, (String)"item")))).then(Commands.argument((String)"maxCount", (ArgumentType)IntegerArgumentType.integer((int)0)).executes(p_323185_ -> _ClearCommand.clearInventory((CommandSourceStack)p_323185_.getSource(), EntityArgument.getPlayers((CommandContext)p_323185_, (String)"targets"), (Predicate<ItemStack>)ItemPredicateArgument.getItemPredicate((CommandContext)p_323185_, (String)"item"), IntegerArgumentType.getInteger((CommandContext)p_323185_, (String)"maxCount")))))));
    }

    private static int clearUnlimited(CommandSourceStack source, Collection<ServerPlayer> targets, Predicate<ItemStack> filter) throws CommandSyntaxException {
        return _ClearCommand.clearInventory(source, targets, filter, -1);
    }

    private static int clearInventory(CommandSourceStack source, Collection<ServerPlayer> targetPlayers, Predicate<ItemStack> itemPredicate, int maxCount) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayer serverplayer : targetPlayers) {
            i += serverplayer.getInventory().clearOrCountMatchingItems(itemPredicate, maxCount, (Container)serverplayer.inventoryMenu.getCraftSlots());
            serverplayer.containerMenu.broadcastChanges();
            serverplayer.inventoryMenu.slotsChanged((Container)serverplayer.getInventory());
        }
        return 1;
    }
}

