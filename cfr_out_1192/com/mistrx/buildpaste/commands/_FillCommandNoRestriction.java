/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class _FillCommandNoRestriction {
    private static final int MAX_FILL_AREA = 32768;
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((p_137392_, p_137393_) -> Component.m_237110_((String)"commands.fill.toobig", (Object[])new Object[]{p_137392_, p_137393_}));
    static final BlockInput HOLLOW_CORE = new BlockInput(Blocks.f_50016_.m_49966_(), Collections.emptySet(), (CompoundTag)null);
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.m_237115_((String)"commands.fill.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> p_214443_, CommandBuildContext p_214444_) {
        p_214443_.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"_fillbuild").requires(p_137384_ -> p_137384_.m_6761_(2))).then(Commands.m_82129_((String)"from", (ArgumentType)BlockPosArgument.m_118239_()).then(Commands.m_82129_((String)"to", (ArgumentType)BlockPosArgument.m_118239_()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.m_82129_((String)"block", (ArgumentType)BlockStateArgument.m_234650_((CommandBuildContext)p_214444_)).executes(p_137405_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_137405_.getSource(), BoundingBox.m_162375_((Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137405_, (String)"from"), (Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137405_, (String)"to")), BlockStateArgument.m_116123_((CommandContext)p_137405_, (String)"block"), Mode.REPLACE, null))).then(((LiteralArgumentBuilder)Commands.m_82127_((String)"replace").executes(p_137403_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_137403_.getSource(), BoundingBox.m_162375_((Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137403_, (String)"from"), (Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137403_, (String)"to")), BlockStateArgument.m_116123_((CommandContext)p_137403_, (String)"block"), Mode.REPLACE, null))).then(Commands.m_82129_((String)"filter", (ArgumentType)BlockPredicateArgument.m_234627_((CommandBuildContext)p_214444_)).executes(p_137401_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_137401_.getSource(), BoundingBox.m_162375_((Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137401_, (String)"from"), (Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137401_, (String)"to")), BlockStateArgument.m_116123_((CommandContext)p_137401_, (String)"block"), Mode.REPLACE, BlockPredicateArgument.m_115573_((CommandContext)p_137401_, (String)"filter")))))).then(Commands.m_82127_((String)"keep").executes(p_137399_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_137399_.getSource(), BoundingBox.m_162375_((Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137399_, (String)"from"), (Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137399_, (String)"to")), BlockStateArgument.m_116123_((CommandContext)p_137399_, (String)"block"), Mode.REPLACE, p_180225_ -> p_180225_.m_61175_().m_46859_(p_180225_.m_61176_()))))).then(Commands.m_82127_((String)"outline").executes(p_137397_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_137397_.getSource(), BoundingBox.m_162375_((Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137397_, (String)"from"), (Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137397_, (String)"to")), BlockStateArgument.m_116123_((CommandContext)p_137397_, (String)"block"), Mode.OUTLINE, null)))).then(Commands.m_82127_((String)"hollow").executes(p_137395_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_137395_.getSource(), BoundingBox.m_162375_((Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137395_, (String)"from"), (Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137395_, (String)"to")), BlockStateArgument.m_116123_((CommandContext)p_137395_, (String)"block"), Mode.HOLLOW, null)))).then(Commands.m_82127_((String)"destroy").executes(p_137382_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_137382_.getSource(), BoundingBox.m_162375_((Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137382_, (String)"from"), (Vec3i)BlockPosArgument.m_118242_((CommandContext)p_137382_, (String)"to")), BlockStateArgument.m_116123_((CommandContext)p_137382_, (String)"block"), Mode.DESTROY, null)))))));
    }

    private static int fillBlocks(CommandSourceStack p_137386_, BoundingBox p_137387_, BlockInput p_137388_, Mode p_137389_, @Nullable Predicate<BlockInWorld> p_137390_) throws CommandSyntaxException {
        int i = p_137387_.m_71056_() * p_137387_.m_71057_() * p_137387_.m_71058_();
        ArrayList list = Lists.newArrayList();
        ServerLevel serverlevel = p_137386_.m_81372_();
        int j = 0;
        for (BlockPos blockpos : BlockPos.m_121976_((int)p_137387_.m_162395_(), (int)p_137387_.m_162396_(), (int)p_137387_.m_162398_(), (int)p_137387_.m_162399_(), (int)p_137387_.m_162400_(), (int)p_137387_.m_162401_())) {
            BlockInput blockinput;
            if (p_137390_ != null && !p_137390_.test(new BlockInWorld((LevelReader)serverlevel, blockpos, true)) || (blockinput = p_137389_.filter.m_138619_(p_137387_, blockpos, p_137388_, serverlevel)) == null) continue;
            BlockEntity blockentity = serverlevel.m_7702_(blockpos);
            Clearable.m_18908_((Object)blockentity);
            if (!blockinput.m_114670_(serverlevel, blockpos, 2)) continue;
            list.add(blockpos.m_7949_());
            ++j;
        }
        for (BlockPos blockpos1 : list) {
            Block block = serverlevel.m_8055_(blockpos1).m_60734_();
            serverlevel.m_6289_(blockpos1, block);
        }
        if (j == 0) {
            throw ERROR_FAILED.create();
        }
        return j;
    }

    static enum Mode {
        REPLACE((p_137433_, p_137434_, p_137435_, p_137436_) -> p_137435_),
        OUTLINE((p_137428_, p_137429_, p_137430_, p_137431_) -> p_137429_.m_123341_() != p_137428_.m_162395_() && p_137429_.m_123341_() != p_137428_.m_162399_() && p_137429_.m_123342_() != p_137428_.m_162396_() && p_137429_.m_123342_() != p_137428_.m_162400_() && p_137429_.m_123343_() != p_137428_.m_162398_() && p_137429_.m_123343_() != p_137428_.m_162401_() ? null : p_137430_),
        HOLLOW((p_137423_, p_137424_, p_137425_, p_137426_) -> p_137424_.m_123341_() != p_137423_.m_162395_() && p_137424_.m_123341_() != p_137423_.m_162399_() && p_137424_.m_123342_() != p_137423_.m_162396_() && p_137424_.m_123342_() != p_137423_.m_162400_() && p_137424_.m_123343_() != p_137423_.m_162398_() && p_137424_.m_123343_() != p_137423_.m_162401_() ? HOLLOW_CORE : p_137425_),
        DESTROY((p_137418_, p_137419_, p_137420_, p_137421_) -> {
            p_137421_.m_46961_(p_137419_, true);
            return p_137420_;
        });

        public final SetBlockCommand.Filter filter;

        private Mode(SetBlockCommand.Filter p_137416_) {
            this.filter = p_137416_;
        }
    }
}

