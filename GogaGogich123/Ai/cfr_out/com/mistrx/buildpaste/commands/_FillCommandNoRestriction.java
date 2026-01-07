/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
import net.minecraft.server.commands.InCommandFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class _FillCommandNoRestriction {
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((p_304218_, p_304219_) -> Component.translatableEscape((String)"commands.fill.toobig", (Object[])new Object[]{p_304218_, p_304219_}));
    static final BlockInput HOLLOW_CORE = new BlockInput(Blocks.AIR.defaultBlockState(), Collections.emptySet(), (CompoundTag)null);
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.translatable((String)"commands.fill.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"_fillbuild").requires(p_137384_ -> p_137384_.hasPermission(2))).then(Commands.argument((String)"from", (ArgumentType)BlockPosArgument.blockPos()).then(Commands.argument((String)"to", (ArgumentType)BlockPosArgument.blockPos()).then(_FillCommandNoRestriction.wrapWithMode(context, Commands.argument((String)"block", (ArgumentType)BlockStateArgument.block((CommandBuildContext)context)), (InCommandFunction<CommandContext<CommandSourceStack>, BlockPos>)((InCommandFunction)p_392711_ -> BlockPosArgument.getLoadedBlockPos((CommandContext)p_392711_, (String)"from")), (InCommandFunction<CommandContext<CommandSourceStack>, BlockPos>)((InCommandFunction)p_392705_ -> BlockPosArgument.getLoadedBlockPos((CommandContext)p_392705_, (String)"to")), (InCommandFunction<CommandContext<CommandSourceStack>, BlockInput>)((InCommandFunction)p_392692_ -> BlockStateArgument.getBlock((CommandContext)p_392692_, (String)"block")), p_392685_ -> null).then(((LiteralArgumentBuilder)Commands.literal((String)"replace").executes(p_392698_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_392698_.getSource(), BoundingBox.fromCorners((Vec3i)BlockPosArgument.getLoadedBlockPos((CommandContext)p_392698_, (String)"from"), (Vec3i)BlockPosArgument.getLoadedBlockPos((CommandContext)p_392698_, (String)"to")), BlockStateArgument.getBlock((CommandContext)p_392698_, (String)"block"), Mode.REPLACE, null, false))).then(_FillCommandNoRestriction.wrapWithMode(context, Commands.argument((String)"filter", (ArgumentType)BlockPredicateArgument.blockPredicate((CommandBuildContext)context)), (InCommandFunction<CommandContext<CommandSourceStack>, BlockPos>)((InCommandFunction)p_392704_ -> BlockPosArgument.getLoadedBlockPos((CommandContext)p_392704_, (String)"from")), (InCommandFunction<CommandContext<CommandSourceStack>, BlockPos>)((InCommandFunction)p_392717_ -> BlockPosArgument.getLoadedBlockPos((CommandContext)p_392717_, (String)"to")), (InCommandFunction<CommandContext<CommandSourceStack>, BlockInput>)((InCommandFunction)p_392718_ -> BlockStateArgument.getBlock((CommandContext)p_392718_, (String)"block")), p_392684_ -> BlockPredicateArgument.getBlockPredicate((CommandContext)p_392684_, (String)"filter")))).then(Commands.literal((String)"keep").executes(p_392691_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_392691_.getSource(), BoundingBox.fromCorners((Vec3i)BlockPosArgument.getLoadedBlockPos((CommandContext)p_392691_, (String)"from"), (Vec3i)BlockPosArgument.getLoadedBlockPos((CommandContext)p_392691_, (String)"to")), BlockStateArgument.getBlock((CommandContext)p_392691_, (String)"block"), Mode.REPLACE, p_180225_ -> p_180225_.getLevel().isEmptyBlock(p_180225_.getPos()), false)))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wrapWithMode(CommandBuildContext p_394423_, ArgumentBuilder<CommandSourceStack, ?> p_393983_, InCommandFunction<CommandContext<CommandSourceStack>, BlockPos> p_397131_, InCommandFunction<CommandContext<CommandSourceStack>, BlockPos> p_397847_, InCommandFunction<CommandContext<CommandSourceStack>, BlockInput> p_397185_, NullableCommandFunction<CommandContext<CommandSourceStack>, Predicate<BlockInWorld>> p_394036_) {
        return p_393983_.executes(p_396538_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_396538_.getSource(), BoundingBox.fromCorners((Vec3i)((Vec3i)p_397131_.apply((Object)p_396538_)), (Vec3i)((Vec3i)p_397847_.apply((Object)p_396538_))), (BlockInput)p_397185_.apply((Object)p_396538_), Mode.REPLACE, (Predicate)p_394036_.apply(p_396538_), false)).then(Commands.literal((String)"outline").executes(p_396553_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_396553_.getSource(), BoundingBox.fromCorners((Vec3i)((Vec3i)p_397131_.apply((Object)p_396553_)), (Vec3i)((Vec3i)p_397847_.apply((Object)p_396553_))), (BlockInput)p_397185_.apply((Object)p_396553_), Mode.OUTLINE, (Predicate)p_394036_.apply(p_396553_), false))).then(Commands.literal((String)"hollow").executes(p_396548_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_396548_.getSource(), BoundingBox.fromCorners((Vec3i)((Vec3i)p_397131_.apply((Object)p_396548_)), (Vec3i)((Vec3i)p_397847_.apply((Object)p_396548_))), (BlockInput)p_397185_.apply((Object)p_396548_), Mode.HOLLOW, (Predicate)p_394036_.apply(p_396548_), false))).then(Commands.literal((String)"destroy").executes(p_396543_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_396543_.getSource(), BoundingBox.fromCorners((Vec3i)((Vec3i)p_397131_.apply((Object)p_396543_)), (Vec3i)((Vec3i)p_397847_.apply((Object)p_396543_))), (BlockInput)p_397185_.apply((Object)p_396543_), Mode.DESTROY, (Predicate)p_394036_.apply(p_396543_), false))).then(Commands.literal((String)"strict").executes(p_396558_ -> _FillCommandNoRestriction.fillBlocks((CommandSourceStack)p_396558_.getSource(), BoundingBox.fromCorners((Vec3i)((Vec3i)p_397131_.apply((Object)p_396558_)), (Vec3i)((Vec3i)p_397847_.apply((Object)p_396558_))), (BlockInput)p_397185_.apply((Object)p_396558_), Mode.REPLACE, (Predicate)p_394036_.apply(p_396558_), true)));
    }

    private static int fillBlocks(CommandSourceStack source, BoundingBox area, BlockInput newBlock, Mode mode, @Nullable Predicate<BlockInWorld> replacingPredicate, boolean p_393689_) throws CommandSyntaxException {
        int i = area.getXSpan() * area.getYSpan() * area.getZSpan();
        int j = source.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);
        ArrayList list = Lists.newArrayList();
        ServerLevel serverlevel = source.getLevel();
        if (serverlevel.isDebug()) {
            throw ERROR_FAILED.create();
        }
        int k = 0;
        Iterator<Object> var11 = BlockPos.betweenClosed((int)area.minX(), (int)area.minY(), (int)area.minZ(), (int)area.maxX(), (int)area.maxY(), (int)area.maxZ()).iterator();
        while (true) {
            BlockInput blockinput;
            BlockPos blockpos2;
            if (!var11.hasNext()) {
                for (BlockPos blockpos2 : list) {
                    Block block = serverlevel.getBlockState(blockpos2).getBlock();
                    serverlevel.updateNeighborsAt(blockpos2, block);
                }
                if (k == 0) {
                    throw ERROR_FAILED.create();
                }
                return k;
            }
            blockpos2 = (BlockPos)var11.next();
            if (replacingPredicate != null && !replacingPredicate.test(new BlockInWorld((LevelReader)serverlevel, blockpos2, true))) continue;
            boolean flag = false;
            if (mode.affector.affect(serverlevel, blockpos2)) {
                flag = true;
            }
            if ((blockinput = mode.filter.filter(area, blockpos2, newBlock, serverlevel)) == null) {
                if (!flag) continue;
                ++k;
                continue;
            }
            if (!blockinput.place(serverlevel, blockpos2, 2 | (p_393689_ ? 816 : 256))) {
                if (!flag) continue;
                ++k;
                continue;
            }
            if (!p_393689_) {
                list.add(blockpos2.immutable());
            }
            ++k;
        }
    }

    @FunctionalInterface
    static interface NullableCommandFunction<T, R> {
        @Nullable
        public R apply(T var1) throws CommandSyntaxException;
    }

    static enum Mode {
        REPLACE(Affector.NOOP, Filter.NOOP),
        OUTLINE(Affector.NOOP, (p_137428_, p_137429_, p_137430_, p_137431_) -> p_137429_.getX() != p_137428_.minX() && p_137429_.getX() != p_137428_.maxX() && p_137429_.getY() != p_137428_.minY() && p_137429_.getY() != p_137428_.maxY() && p_137429_.getZ() != p_137428_.minZ() && p_137429_.getZ() != p_137428_.maxZ() ? null : p_137430_),
        HOLLOW(Affector.NOOP, (p_137423_, p_137424_, p_137425_, p_137426_) -> p_137424_.getX() != p_137423_.minX() && p_137424_.getX() != p_137423_.maxX() && p_137424_.getY() != p_137423_.minY() && p_137424_.getY() != p_137423_.maxY() && p_137424_.getZ() != p_137423_.minZ() && p_137424_.getZ() != p_137423_.maxZ() ? HOLLOW_CORE : p_137425_),
        DESTROY((p_392719_, p_392720_) -> p_392719_.destroyBlock(p_392720_, true), Filter.NOOP);

        public final Filter filter;
        public final Affector affector;

        private Mode(Affector p_394156_, Filter p_393770_) {
            this.affector = p_394156_;
            this.filter = p_393770_;
        }
    }

    @FunctionalInterface
    public static interface Affector {
        public static final Affector NOOP = (p_393846_, p_393551_) -> false;

        public boolean affect(ServerLevel var1, BlockPos var2);
    }

    @FunctionalInterface
    public static interface Filter {
        public static final Filter NOOP = (p_394191_, p_393566_, p_394115_, p_394030_) -> p_394115_;

        @Nullable
        public BlockInput filter(BoundingBox var1, BlockPos var2, BlockInput var3, ServerLevel var4);
    }
}

