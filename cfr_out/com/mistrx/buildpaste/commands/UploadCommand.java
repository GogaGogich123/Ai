/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.items.position_selector.PositionSelectorHandler;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mistrx.buildpaste.util.Functions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

public final class UploadCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"upload").executes(source -> UploadCommand.upload((CommandSourceStack)source.getSource(), ""))).then(Commands.argument((String)"build name", (ArgumentType)MessageArgument.message()).executes(source -> UploadCommand.upload((CommandSourceStack)source.getSource(), MessageArgument.getMessage((CommandContext)source, (String)"build name").getString()))));
    }

    public static int upload(CommandSourceStack source, String buildName) {
        double zStep;
        double zEnd;
        double zStart;
        double xStep;
        double xEnd;
        double xStart;
        ServerPlayer player;
        Level world = null;
        try {
            player = source.getPlayerOrException();
            world = player.level();
        }
        catch (CommandSyntaxException e) {
            e.printStackTrace();
            return 0;
        }
        Vector3d pos1 = PlayerDataManager.getOrCreatePlayerData((Player)player).getUploadingDataStore().getPos1();
        Vector3d pos2 = PlayerDataManager.getOrCreatePlayerData((Player)player).getUploadingDataStore().getPos2();
        if (pos1 == null && pos2 == null) {
            source.sendSuccess(() -> Component.translatable((String)"commands.upload.help"), true);
            return 0;
        }
        if (pos1 == null) {
            source.sendFailure((Component)Component.translatable((String)"commands.upload.error.pos", (Object[])new Object[]{"pos1", ChatFormatting.RED}));
            return 1;
        }
        if (pos2 == null) {
            source.sendFailure((Component)Component.translatable((String)"commands.upload.error.pos", (Object[])new Object[]{"pos2", ChatFormatting.RED}));
            return 1;
        }
        double xSmall = Math.min(pos1.x, pos2.x);
        double xLarge = Math.max(pos1.x, pos2.x);
        double ySmall = Math.min(pos1.y, pos2.y);
        double yLarge = Math.max(pos1.y, pos2.y);
        double zSmall = Math.min(pos1.z, pos2.z);
        double zLarge = Math.max(pos1.z, pos2.z);
        List<Integer> size = List.of(Integer.valueOf((int)(xLarge - xSmall + 1.0)), Integer.valueOf((int)(yLarge - ySmall + 1.0)), Integer.valueOf((int)(zLarge - zSmall + 1.0)));
        ArrayList<Object> blocks = new ArrayList<Object>();
        ArrayList<String> blockData = new ArrayList<String>();
        JsonObject nbtDataObject = new JsonObject();
        String lookDirection = "south";
        try {
            lookDirection = Functions.getPlayerStringLookDirection((Player)source.getPlayerOrException());
        }
        catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        PositionSelectorHandler.uploadSize = (int)(Math.abs(pos1.x - pos2.x + 1.0) * Math.abs(pos1.y - pos2.y + 1.0) * Math.abs(pos1.z - pos2.z + 1.0));
        PositionSelectorHandler.isUploading = true;
        int index = 0;
        if (lookDirection.equals("north") || lookDirection.equals("west")) {
            xStart = xLarge;
            xEnd = xSmall;
            xStep = -1.0;
        } else {
            xStart = xSmall;
            xEnd = xLarge;
            xStep = 1.0;
        }
        if (lookDirection.equals("north") || lookDirection.equals("east")) {
            zStart = zLarge;
            zEnd = zSmall;
            zStep = -1.0;
        } else {
            zStart = zSmall;
            zEnd = zLarge;
            zStep = 1.0;
        }
        double x = xStart;
        while (xStep > 0.0 ? x <= xEnd : x >= zEnd) {
            for (double y = ySmall; y <= yLarge; y += 1.0) {
                double z = zStart;
                while (zStep > 0.0 ? z <= zEnd : z >= zEnd) {
                    BlockEntity blockEntity;
                    BlockPos blockPos = new BlockPos((int)x, (int)y, (int)z);
                    BlockState state = world.getBlockState(blockPos);
                    Block block = state.getBlock();
                    Integer blockID = Functions.getBlockIdByName(Functions.getBlocknameFromBlock(block).replace("minecraft:", ""));
                    if (blockID != null) {
                        blocks.add(blockID);
                    } else {
                        blocks.add("\"" + Functions.getBlocknameFromBlock(block) + "\"");
                    }
                    String data = Functions.getBlockData(world, new Vector3d((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()));
                    if (data != null) {
                        blockData.add("\"" + data + "\"");
                    } else {
                        blockData.add(null);
                    }
                    if (world.getBlockEntity(blockPos) != null && (blockEntity = world.getBlockEntity(blockPos)) != null) {
                        CompoundTag tag = blockEntity.saveWithFullMetadata((HolderLookup.Provider)world.registryAccess());
                        String nbt = tag.toString();
                        nbtDataObject.add(Integer.toString(index), (JsonElement)new JsonPrimitive(nbt));
                    }
                    ++index;
                    z += zStep;
                }
            }
            x += xStep;
        }
        String cleanedBuildName = buildName.replaceAll("/", " ");
        String buildID = "";
        try {
            buildID = Firebase.sendStructureData((Player)player, blocks, blockData, nbtDataObject.toString(), size, lookDirection, cleanedBuildName);
        }
        catch (Exception e) {
            source.sendSuccess(() -> Component.translatable((String)"commands.upload.error.size", (Object[])new Object[]{PositionSelectorHandler.uploadSize}), true);
        }
        PositionSelectorHandler.isUploading = false;
        if (Objects.equals(buildID, "-")) {
            if (PositionSelectorHandler.uploadSize > 1000000) {
                source.sendSuccess(() -> Component.translatable((String)"commands.upload.error.size", (Object[])new Object[]{PositionSelectorHandler.uploadSize}), true);
            } else {
                source.sendSuccess(() -> Component.translatable((String)"commands.upload.error.random"), true);
            }
        } else {
            PlayerDataManager.getOrCreatePlayerData((Player)player).setUploadedBuildID(buildID);
            PlayerDataManager.getOrCreatePlayerData((Player)player).getUploadingDataStore().setPos1(null);
            PlayerDataManager.getOrCreatePlayerData((Player)player).getUploadingDataStore().setPos2(null);
            String url = "https://buildpaste.net/upload?build=" + buildID;
            SimpleComponent.TextBuilder publish = SimpleComponent.text("Publish", ChatFormatting.GREEN).clickOpenUrl(url).hoverText("Publish your build and make it available for anyone!", ChatFormatting.GREEN);
            SimpleComponent.TextBuilder copy = SimpleComponent.text("Copy", ChatFormatting.AQUA).clickRunCommand("/_copybuildid " + buildID).hoverText("Copy the build", ChatFormatting.AQUA);
            SimpleComponent.TextBuilder paste = SimpleComponent.text("Paste", ChatFormatting.LIGHT_PURPLE).clickRunCommand("/paste " + buildID).hoverText("Paste your build", ChatFormatting.LIGHT_PURPLE);
            source.sendSuccess(() -> Component.translatable((String)"commands.upload.success", (Object[])new Object[]{publish, copy, paste}), true);
        }
        return 1;
    }
}

