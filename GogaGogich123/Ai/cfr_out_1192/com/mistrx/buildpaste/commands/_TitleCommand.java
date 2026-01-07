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
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class _TitleCommand {
    public static void register(CommandDispatcher<CommandSourceStack> p_139103_) {
        p_139103_.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"_titlenoresponse").requires(p_139107_ -> p_139107_.m_6761_(2))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.m_82129_((String)"targets", (ArgumentType)EntityArgument.m_91470_()).then(Commands.m_82127_((String)"clear").executes(p_139134_ -> _TitleCommand.clearTitle((CommandSourceStack)p_139134_.getSource(), EntityArgument.m_91477_((CommandContext)p_139134_, (String)"targets"))))).then(Commands.m_82127_((String)"reset").executes(p_139132_ -> _TitleCommand.resetTitle((CommandSourceStack)p_139132_.getSource(), EntityArgument.m_91477_((CommandContext)p_139132_, (String)"targets"))))).then(Commands.m_82127_((String)"title").then(Commands.m_82129_((String)"title", (ArgumentType)ComponentArgument.m_87114_()).executes(p_139130_ -> _TitleCommand.showTitle((CommandSourceStack)p_139130_.getSource(), EntityArgument.m_91477_((CommandContext)p_139130_, (String)"targets"), ComponentArgument.m_87117_((CommandContext)p_139130_, (String)"title"), "title", ClientboundSetTitleTextPacket::new))))).then(Commands.m_82127_((String)"subtitle").then(Commands.m_82129_((String)"title", (ArgumentType)ComponentArgument.m_87114_()).executes(p_139128_ -> _TitleCommand.showTitle((CommandSourceStack)p_139128_.getSource(), EntityArgument.m_91477_((CommandContext)p_139128_, (String)"targets"), ComponentArgument.m_87117_((CommandContext)p_139128_, (String)"title"), "subtitle", ClientboundSetSubtitleTextPacket::new))))).then(Commands.m_82127_((String)"actionbar").then(Commands.m_82129_((String)"title", (ArgumentType)ComponentArgument.m_87114_()).executes(p_139123_ -> _TitleCommand.showTitle((CommandSourceStack)p_139123_.getSource(), EntityArgument.m_91477_((CommandContext)p_139123_, (String)"targets"), ComponentArgument.m_87117_((CommandContext)p_139123_, (String)"title"), "actionbar", ClientboundSetActionBarTextPacket::new))))).then(Commands.m_82127_((String)"times").then(Commands.m_82129_((String)"fadeIn", (ArgumentType)IntegerArgumentType.integer((int)0)).then(Commands.m_82129_((String)"stay", (ArgumentType)IntegerArgumentType.integer((int)0)).then(Commands.m_82129_((String)"fadeOut", (ArgumentType)IntegerArgumentType.integer((int)0)).executes(p_139105_ -> _TitleCommand.setTimes((CommandSourceStack)p_139105_.getSource(), EntityArgument.m_91477_((CommandContext)p_139105_, (String)"targets"), IntegerArgumentType.getInteger((CommandContext)p_139105_, (String)"fadeIn"), IntegerArgumentType.getInteger((CommandContext)p_139105_, (String)"stay"), IntegerArgumentType.getInteger((CommandContext)p_139105_, (String)"fadeOut")))))))));
    }

    private static int clearTitle(CommandSourceStack p_139109_, Collection<ServerPlayer> p_139110_) {
        ClientboundClearTitlesPacket clientboundcleartitlespacket = new ClientboundClearTitlesPacket(false);
        for (ServerPlayer serverplayer : p_139110_) {
            serverplayer.f_8906_.m_9829_((Packet)clientboundcleartitlespacket);
        }
        return p_139110_.size();
    }

    private static int resetTitle(CommandSourceStack p_139125_, Collection<ServerPlayer> p_139126_) {
        ClientboundClearTitlesPacket clientboundcleartitlespacket = new ClientboundClearTitlesPacket(true);
        for (ServerPlayer serverplayer : p_139126_) {
            serverplayer.f_8906_.m_9829_((Packet)clientboundcleartitlespacket);
        }
        return p_139126_.size();
    }

    private static int showTitle(CommandSourceStack p_142781_, Collection<ServerPlayer> p_142782_, Component p_142783_, String p_142784_, Function<Component, Packet<?>> p_142785_) throws CommandSyntaxException {
        for (ServerPlayer serverplayer : p_142782_) {
            serverplayer.f_8906_.m_9829_(p_142785_.apply((Component)ComponentUtils.m_130731_((CommandSourceStack)p_142781_, (Component)p_142783_, (Entity)serverplayer, (int)0)));
        }
        return p_142782_.size();
    }

    private static int setTimes(CommandSourceStack p_139112_, Collection<ServerPlayer> p_139113_, int p_139114_, int p_139115_, int p_139116_) {
        ClientboundSetTitlesAnimationPacket clientboundsettitlesanimationpacket = new ClientboundSetTitlesAnimationPacket(p_139114_, p_139115_, p_139116_);
        for (ServerPlayer serverplayer : p_139113_) {
            serverplayer.f_8906_.m_9829_((Packet)clientboundsettitlesanimationpacket);
        }
        return p_139113_.size();
    }
}

