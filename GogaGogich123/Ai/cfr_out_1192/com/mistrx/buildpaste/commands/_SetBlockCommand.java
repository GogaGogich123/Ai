/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class _SetBlockCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.m_237115_((String)"commands.setblock.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> p_214731_, CommandBuildContext p_214732_) {
        p_214731_.register((LiteralArgumentBuilder)Commands.m_82127_((String)"_setblocknoresponse").then(Commands.m_82129_((String)"pos", (ArgumentType)BlockPosArgument.m_118239_()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.m_82129_((String)"block", (ArgumentType)BlockStateArgument.m_234650_((CommandBuildContext)p_214732_)).executes(p_138618_ -> _SetBlockCommand.setBlock((CommandSourceStack)p_138618_.getSource(), BlockPosArgument.m_118242_((CommandContext)p_138618_, (String)"pos"), BlockStateArgument.m_116123_((CommandContext)p_138618_, (String)"block"), SetBlockCommand.Mode.REPLACE, null))).then(Commands.m_82127_((String)"destroy").executes(p_138616_ -> _SetBlockCommand.setBlock((CommandSourceStack)p_138616_.getSource(), BlockPosArgument.m_118242_((CommandContext)p_138616_, (String)"pos"), BlockStateArgument.m_116123_((CommandContext)p_138616_, (String)"block"), SetBlockCommand.Mode.DESTROY, null)))).then(Commands.m_82127_((String)"keep").executes(p_138614_ -> _SetBlockCommand.setBlock((CommandSourceStack)p_138614_.getSource(), BlockPosArgument.m_118242_((CommandContext)p_138614_, (String)"pos"), BlockStateArgument.m_116123_((CommandContext)p_138614_, (String)"block"), SetBlockCommand.Mode.REPLACE, p_180517_ -> p_180517_.m_61175_().m_46859_(p_180517_.m_61176_()))))).then(Commands.m_82127_((String)"replace").executes(p_138604_ -> _SetBlockCommand.setBlock((CommandSourceStack)p_138604_.getSource(), BlockPosArgument.m_118242_((CommandContext)p_138604_, (String)"pos"), BlockStateArgument.m_116123_((CommandContext)p_138604_, (String)"block"), SetBlockCommand.Mode.REPLACE, null))))));
    }

    private static int setBlock(CommandSourceStack p_138608_, BlockPos p_138609_, BlockInput p_138610_, SetBlockCommand.Mode p_138611_, @Nullable Predicate<BlockInWorld> p_138612_) throws CommandSyntaxException {
        boolean flag;
        ServerLevel serverlevel = p_138608_.m_81372_();
        if (p_138612_ != null && !p_138612_.test(new BlockInWorld((LevelReader)serverlevel, p_138609_, true))) {
            throw ERROR_FAILED.create();
        }
        if (p_138611_ == SetBlockCommand.Mode.DESTROY) {
            serverlevel.m_46961_(p_138609_, true);
            flag = !p_138610_.m_114669_().m_60795_() || !serverlevel.m_8055_(p_138609_).m_60795_();
        } else {
            BlockEntity blockentity = serverlevel.m_7702_(p_138609_);
            Clearable.m_18908_((Object)blockentity);
            flag = true;
        }
        if (flag && !p_138610_.m_114670_(serverlevel, p_138609_, 2)) {
            throw ERROR_FAILED.create();
        }
        serverlevel.m_6289_(p_138609_, p_138610_.m_114669_().m_60734_());
        return 1;
    }

    public static enum Mode {
        REPLACE,
        DESTROY;

    }

    public static interface Filter {
        @Nullable
        public BlockInput filter(BoundingBox var1, BlockPos var2, BlockInput var3, ServerLevel var4);
    }
}

