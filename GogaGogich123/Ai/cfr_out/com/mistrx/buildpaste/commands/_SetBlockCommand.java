/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class _SetBlockCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        Predicate<BlockInWorld> predicate = p_180517_ -> p_180517_.getLevel().isEmptyBlock(p_180517_.getPos());
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"_setblocknoresponse").requires(p_138606_ -> p_138606_.hasPermission(2))).then(Commands.argument((String)"pos", (ArgumentType)BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument((String)"block", (ArgumentType)BlockStateArgument.block((CommandBuildContext)context)).executes(p_392756_ -> _SetBlockCommand.setBlock((CommandSourceStack)p_392756_.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext)p_392756_, (String)"pos"), BlockStateArgument.getBlock((CommandContext)p_392756_, (String)"block"), Mode.REPLACE, null, false))).then(Commands.literal((String)"destroy").executes(p_392755_ -> _SetBlockCommand.setBlock((CommandSourceStack)p_392755_.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext)p_392755_, (String)"pos"), BlockStateArgument.getBlock((CommandContext)p_392755_, (String)"block"), Mode.DESTROY, null, false)))).then(Commands.literal((String)"keep").executes(p_392758_ -> _SetBlockCommand.setBlock((CommandSourceStack)p_392758_.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext)p_392758_, (String)"pos"), BlockStateArgument.getBlock((CommandContext)p_392758_, (String)"block"), Mode.REPLACE, predicate, false)))).then(Commands.literal((String)"replace").executes(p_392760_ -> _SetBlockCommand.setBlock((CommandSourceStack)p_392760_.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext)p_392760_, (String)"pos"), BlockStateArgument.getBlock((CommandContext)p_392760_, (String)"block"), Mode.REPLACE, null, false)))).then(Commands.literal((String)"strict").executes(p_392759_ -> _SetBlockCommand.setBlock((CommandSourceStack)p_392759_.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext)p_392759_, (String)"pos"), BlockStateArgument.getBlock((CommandContext)p_392759_, (String)"block"), Mode.REPLACE, null, true))))));
    }

    private static int setBlock(CommandSourceStack source, BlockPos pos, BlockInput state, Mode mode, @Nullable Predicate<BlockInWorld> predicate, boolean p_393976_) throws CommandSyntaxException {
        ServerLevel serverlevel = source.getLevel();
        if (!serverlevel.isDebug() && (predicate == null || predicate.test(new BlockInWorld((LevelReader)serverlevel, pos, true)))) {
            boolean flag;
            if (mode == Mode.DESTROY) {
                serverlevel.destroyBlock(pos, true);
                flag = !state.getState().isAir() || !serverlevel.getBlockState(pos).isAir();
            } else {
                flag = true;
            }
            if (!flag || state.place(serverlevel, pos, 2 | (p_393976_ ? 816 : 256))) {
                if (!p_393976_) {
                    serverlevel.updateNeighborsAt(pos, state.getState().getBlock());
                }
                return 1;
            }
        }
        return 0;
    }

    public static enum Mode {
        REPLACE,
        DESTROY;

    }
}

