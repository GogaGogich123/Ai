/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.events.ModClientEvents;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.util.Functions;
import com.mistrx.buildpaste.util.Variables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Vector3d;
import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class UploadCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"upload").requires(source -> source.m_6761_(4))).executes(source -> UploadCommand.upload((CommandSourceStack)source.getSource(), ""))).then(Commands.m_82129_((String)"build name", (ArgumentType)MessageArgument.m_96832_()).executes(source -> UploadCommand.upload((CommandSourceStack)source.getSource(), MessageArgument.m_96835_((CommandContext)source, (String)"build name").getString()))));
    }

    public static int upload(CommandSourceStack source, String buildname) {
        String buildname_;
        String buildID;
        double zSmall;
        double zLarge;
        double ySmall;
        double yLarge;
        double xSmall;
        double xLarge;
        Level world = null;
        try {
            Functions.setPlayerVariables((Player)source.m_81375_());
            world = source.m_81375_().m_20193_();
        }
        catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        Vector3d pos1 = ModClientEvents.pos1;
        Vector3d pos2 = ModClientEvents.pos2;
        if (pos1 == null && pos2 == null) {
            source.m_81354_((Component)Component.m_237115_((String)"commands.upload.help"), true);
            return 0;
        }
        if (pos1.equals(pos2)) {
            source.m_81352_((Component)Component.m_237110_((String)"commands.upload.error.samepos", (Object[])new Object[]{ChatFormatting.RED}));
            return 0;
        }
        if (pos1 == null) {
            source.m_81352_((Component)Component.m_237110_((String)"commands.upload.error.pos", (Object[])new Object[]{"pos1", ChatFormatting.RED}));
            return 1;
        }
        if (pos2 == null) {
            source.m_81352_((Component)Component.m_237110_((String)"commands.upload.error.pos", (Object[])new Object[]{"pos2", ChatFormatting.RED}));
            return 1;
        }
        ArrayList<Integer> size = new ArrayList<Integer>();
        if (pos1.f_86214_ >= pos2.f_86214_) {
            xLarge = pos1.f_86214_;
            xSmall = pos2.f_86214_;
        } else {
            xLarge = pos2.f_86214_;
            xSmall = pos1.f_86214_;
        }
        if (pos1.f_86215_ >= pos2.f_86215_) {
            yLarge = pos1.f_86215_;
            ySmall = pos2.f_86215_;
        } else {
            yLarge = pos2.f_86215_;
            ySmall = pos1.f_86215_;
        }
        if (pos1.f_86216_ >= pos2.f_86216_) {
            zLarge = pos1.f_86216_;
            zSmall = pos2.f_86216_;
        } else {
            zLarge = pos2.f_86216_;
            zSmall = pos1.f_86216_;
        }
        size.add((int)Math.round(xLarge + 1.0 - xSmall));
        size.add((int)Math.round(yLarge + 1.0 - ySmall));
        size.add((int)Math.round(zLarge + 1.0 - zSmall));
        ArrayList<Object> blocks = new ArrayList<Object>();
        ArrayList<String> blockData = new ArrayList<String>();
        Object nbtData = "{";
        String lookDirection = "south";
        try {
            lookDirection = Functions.getLookDirection(source.m_81375_().m_5675_(1.0f));
        }
        catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        Integer index = 0;
        if (lookDirection.equals("north")) {
            for (x = xLarge; x > xSmall - 1.0; x -= 1.0) {
                for (y = ySmall; y < yLarge + 1.0; y += 1.0) {
                    for (z = zLarge; z > zSmall - 1.0; z -= 1.0) {
                        Object nbt;
                        blockPos = new BlockPos(x, y, z);
                        state = world.m_8055_(blockPos);
                        block = state.m_60734_();
                        blockID = Functions.getBlockIdByName(Functions.getBlocknameFromBlock(block).replace("minecraft:", ""));
                        if (blockID != null) {
                            blocks.add(blockID);
                        } else {
                            blocks.add("\"" + Functions.getBlocknameFromBlock(block) + "\"");
                        }
                        data = Functions.getData(world, new Vector3d((double)blockPos.m_123341_(), (double)blockPos.m_123342_(), (double)blockPos.m_123343_()));
                        if (data != null) {
                            blockData.add("\"" + data + "\"");
                        } else {
                            blockData.add(null);
                        }
                        if (world.m_7702_(blockPos) != null) {
                            nbt = world.m_7702_(blockPos).serializeNBT().m_7916_();
                            String jsonNbt = "\"" + index.toString() + "\": \"" + ((String)nbt).replaceAll("\"", "'").replaceAll("}'", "}").replaceAll("'\\{", "\\{") + "\"";
                            nbtData = ((String)nbtData).equals("{") ? (String)nbtData + jsonNbt : (String)nbtData + "," + jsonNbt;
                        }
                        nbt = index;
                        index = index + 1;
                    }
                }
            }
        } else if (lookDirection.equals("east")) {
            for (x = xSmall; x < xLarge + 1.0; x += 1.0) {
                for (y = ySmall; y < yLarge + 1.0; y += 1.0) {
                    for (z = zLarge; z > zSmall - 1.0; z -= 1.0) {
                        Object nbt;
                        blockPos = new BlockPos(x, y, z);
                        state = world.m_8055_(blockPos);
                        block = state.m_60734_();
                        blockID = Functions.getBlockIdByName(Functions.getBlocknameFromBlock(block).replace("minecraft:", ""));
                        if (blockID != null) {
                            blocks.add(blockID);
                        } else {
                            blocks.add("\"" + Functions.getBlocknameFromBlock(block) + "\"");
                        }
                        data = Functions.getData(world, new Vector3d((double)blockPos.m_123341_(), (double)blockPos.m_123342_(), (double)blockPos.m_123343_()));
                        if (data != null) {
                            blockData.add("\"" + data + "\"");
                        } else {
                            blockData.add(null);
                        }
                        if (world.m_7702_(blockPos) != null) {
                            nbt = world.m_7702_(blockPos).serializeNBT().m_7916_();
                            String jsonNbt = "\"" + index.toString() + "\": \"" + ((String)nbt).replaceAll("\"", "'").replaceAll("}'", "}").replaceAll("'\\{", "\\{") + "\"";
                            nbtData = ((String)nbtData).equals("{") ? (String)nbtData + jsonNbt : (String)nbtData + "," + jsonNbt;
                        }
                        nbt = index;
                        index = index + 1;
                    }
                }
            }
        } else if (lookDirection.equals("south")) {
            for (x = xSmall; x < xLarge + 1.0; x += 1.0) {
                for (y = ySmall; y < yLarge + 1.0; y += 1.0) {
                    for (z = zSmall; z < zLarge + 1.0; z += 1.0) {
                        Object nbt;
                        blockPos = new BlockPos(x, y, z);
                        state = world.m_8055_(blockPos);
                        block = state.m_60734_();
                        blockID = Functions.getBlockIdByName(Functions.getBlocknameFromBlock(block).replace("minecraft:", ""));
                        if (blockID != null) {
                            blocks.add(blockID);
                        } else {
                            blocks.add("\"" + Functions.getBlocknameFromBlock(block) + "\"");
                        }
                        data = Functions.getData(world, new Vector3d((double)blockPos.m_123341_(), (double)blockPos.m_123342_(), (double)blockPos.m_123343_()));
                        if (data != null) {
                            blockData.add("\"" + data + "\"");
                        } else {
                            blockData.add(null);
                        }
                        if (world.m_7702_(blockPos) != null) {
                            nbt = world.m_7702_(blockPos).serializeNBT().m_7916_();
                            String jsonNbt = "\"" + index.toString() + "\": \"" + ((String)nbt).replaceAll("\"", "'").replaceAll("}'", "}").replaceAll("'\\{", "\\{") + "\"";
                            nbtData = ((String)nbtData).equals("{") ? (String)nbtData + jsonNbt : (String)nbtData + "," + jsonNbt;
                        }
                        nbt = index;
                        index = index + 1;
                    }
                }
            }
        } else if (lookDirection.equals("west")) {
            for (x = xLarge; x > xSmall - 1.0; x -= 1.0) {
                for (y = ySmall; y < yLarge + 1.0; y += 1.0) {
                    for (z = zSmall; z < zLarge + 1.0; z += 1.0) {
                        blockPos = new BlockPos(x, y, z);
                        state = world.m_8055_(blockPos);
                        block = state.m_60734_();
                        blockID = Functions.getBlockIdByName(Functions.getBlocknameFromBlock(block).replace("minecraft:", ""));
                        if (blockID != null) {
                            blocks.add(blockID);
                        } else {
                            blocks.add("\"" + Functions.getBlocknameFromBlock(block) + "\"");
                        }
                        data = Functions.getData(world, new Vector3d((double)blockPos.m_123341_(), (double)blockPos.m_123342_(), (double)blockPos.m_123343_()));
                        if (data != null) {
                            blockData.add("\"" + data + "\"");
                        } else {
                            blockData.add(null);
                        }
                        if (world.m_7702_(blockPos) != null) {
                            String nbt = world.m_7702_(blockPos).serializeNBT().m_7916_();
                            String jsonNbt = "\"" + index.toString() + "\": \"" + nbt.replaceAll("\"", "'").replaceAll("}'", "}").replaceAll("'\\{", "\\{") + "\"";
                            nbtData = ((String)nbtData).equals("{") ? (String)nbtData + jsonNbt : (String)nbtData + "," + jsonNbt;
                        }
                        Integer n = index;
                        index = index + 1;
                    }
                }
            }
        } else {
            source.m_81352_((Component)Component.m_237110_((String)"commands.upload.error.size", (Object[])new Object[]{ChatFormatting.RED}));
            return 1;
        }
        if ((buildID = Firebase.sendStructureData(blocks, blockData, (String)(nbtData = (String)nbtData + "}"), size, lookDirection, buildname_ = buildname.replaceAll("/", " "))) == "-") {
            source.m_81352_((Component)Component.m_237110_((String)"commands.upload.error.size", (Object[])new Object[]{ChatFormatting.RED}));
        } else {
            Variables.uploadedBuildID = buildID;
            ModClientEvents.pos1 = null;
            ModClientEvents.pos2 = null;
            String url = Variables.url + "/upload?build=" + buildID;
            MutableComponent publish = Component.m_237113_((String)"Publish");
            Style publishStyle = Style.f_131099_;
            publishStyle = publishStyle.m_131157_(ChatFormatting.GREEN);
            publishStyle = publishStyle.m_131142_(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            publishStyle = publishStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Publish your Build and make it available for anyone! That would be pretty nice :)").m_130940_(ChatFormatting.GREEN)));
            publish.m_6270_(publishStyle);
            MutableComponent copy = Component.m_237113_((String)"Copy");
            Style copyStyle = Style.f_131099_;
            copyStyle = copyStyle.m_131157_(ChatFormatting.AQUA);
            copyStyle = copyStyle.m_131142_(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/_copybuildid"));
            copyStyle = copyStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Copy your Build").m_130940_(ChatFormatting.AQUA)));
            copy.m_6270_(copyStyle);
            MutableComponent paste = Component.m_237113_((String)"Paste");
            Style pasteStyle = Style.f_131099_;
            pasteStyle = pasteStyle.m_131157_(ChatFormatting.LIGHT_PURPLE);
            pasteStyle = pasteStyle.m_131142_(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/paste " + buildID));
            pasteStyle = pasteStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Paste your Build").m_130940_(ChatFormatting.LIGHT_PURPLE)));
            paste.m_6270_(pasteStyle);
            MutableComponent print = Component.m_237113_((String)"3D Print");
            Style printStyle = Style.f_131099_;
            printStyle = printStyle.m_131157_(ChatFormatting.GOLD);
            printStyle = printStyle.m_131142_(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://blockprints.net/print/build/" + buildID));
            printStyle = printStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Create a real-life, 3D printed replica of your uploaded build").m_130940_(ChatFormatting.GOLD)));
            print.m_6270_(printStyle);
            source.m_81354_((Component)Component.m_237110_((String)"commands.upload.success", (Object[])new Object[]{publish, copy, paste, print}), true);
        }
        return 1;
    }
}

