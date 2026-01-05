/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.util.Functions;
import com.mistrx.buildpaste.util.Variables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Vector3d;
import java.util.HashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ConstructCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"construct").executes(source -> ConstructCommand.constructCommand((CommandSourceStack)source.getSource(), null))).then(Commands.m_82129_((String)"first argument", (ArgumentType)StringArgumentType.string()).executes(source -> ConstructCommand.constructCommand((CommandSourceStack)source.getSource(), StringArgumentType.getString((CommandContext)source, (String)"first argument")))));
    }

    public static int constructCommand(CommandSourceStack source, String firstarg) {
        String[] args = new String[]{firstarg, null};
        BuildPasteMod.LOGGER.info("firstarg: " + firstarg);
        if (firstarg != null && firstarg.equals("constructbuild")) {
            String direction = null;
            try {
                direction = Functions.getLookDirection(source.m_81375_().m_5675_(1.0f));
                Functions.setPlayerVariables((Player)source.m_81375_());
                if (Functions.sendInvalidUUIDMessage((Player)source.m_81375_()).booleanValue()) {
                    source.m_81352_((Component)Component.m_237115_((String)"util.doesnt-have-valid-uuid"));
                }
            }
            catch (CommandSyntaxException e) {
                BuildPasteMod.LOGGER.info("Error in try catch at constructCommand");
                e.printStackTrace();
            }
            Vector3d pos = new Vector3d(Math.floor(source.m_81371_().f_82479_), Math.floor(source.m_81371_().f_82480_), Math.floor(source.m_81371_().f_82481_));
            Functions.pasteCurrentBuilding(pos, direction, "dontplaceair", false, true);
            source.m_81354_((Component)Component.m_237115_((String)"commands.construct.success"), true);
            return 1;
        }
        Variables.playerItems = new HashMap();
        Variables.buildBlocks = new HashMap();
        BuildPasteMod.LOGGER.info("yes");
        try {
            Functions.setPlayerVariables((Player)source.m_81375_());
            ServerPlayer player = source.m_81375_();
            NonNullList inventory = player.m_150109_().f_35974_;
            Integer inventorySize = inventory.size();
            for (int i = 0; i < inventorySize; ++i) {
                String[] itemArray = ((ItemStack)inventory.get(i)).toString().split(" ");
                String itemName = "minecraft:" + itemArray[1];
                Integer itemAmount = Integer.parseInt(itemArray[0]);
                Variables.playerItems.merge(itemName, itemAmount, Integer::sum);
            }
        }
        catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        BuildPasteMod.LOGGER.info("Construct Command executed");
        String response = Functions.setBuilding(null, "south", args, false);
        if (response.equals("error")) {
            source.m_81352_((Component)Component.m_237115_((String)"commands.paste.error"));
            return 0;
        }
        if (response.equals("random-error")) {
            source.m_81352_((Component)Component.m_237115_((String)"commands.paste.random-error"));
            return 0;
        }
        if (response.equals("error-404")) {
            source.m_81352_((Component)Component.m_237115_((String)"commands.paste.error-404"));
            return 0;
        }
        BuildPasteMod.LOGGER.info(response);
        for (int i = 0; i < Firebase.blockIDs.size(); ++i) {
            String block = Functions.getItemFromBlock(Functions.getBlockByIdOrName(Firebase.blockIDs.get(i)));
            Variables.buildBlocks.merge(block, 1, Integer::sum);
        }
        Variables.buildBlocks.remove("minecraft:air");
        Variables.playerItems.remove("minecraft:air");
        Boolean hasAllRequiredMaterials = true;
        Boolean hasAnyRequiredMaterials = false;
        Object requiredMaterials = "Needed Materials:\n";
        Object[] buildBlocksKeys = Variables.buildBlocks.keySet().toArray();
        for (int i = 0; i < buildBlocksKeys.length; ++i) {
            String key = buildBlocksKeys[i].toString();
            Integer amount = Variables.buildBlocks.get(key);
            Object endText = "";
            ChatFormatting color = ChatFormatting.WHITE;
            if (Variables.playerItems.containsKey(key)) {
                hasAnyRequiredMaterials = true;
                if (Variables.playerItems.get(key) >= amount) {
                    color = ChatFormatting.GREEN;
                } else {
                    endText = " (" + Variables.playerItems.get(key) + "/" + amount.toString() + ")";
                    hasAllRequiredMaterials = false;
                }
            } else {
                hasAllRequiredMaterials = false;
            }
            requiredMaterials = (String)requiredMaterials + color + amount.toString() + " x " + key.replace("minecraft:", "") + (String)endText + "\n";
        }
        String constructBuildCommand = "/construct constructbuild";
        source.m_81354_((Component)Component.m_237113_((String)requiredMaterials), true);
        if (hasAllRequiredMaterials.booleanValue()) {
            construct = Component.m_237113_((String)"Construct");
            constructStyle = Style.f_131099_;
            constructStyle = constructStyle.m_131157_(ChatFormatting.LIGHT_PURPLE);
            constructStyle = constructStyle.m_131142_(new ClickEvent(ClickEvent.Action.RUN_COMMAND, constructBuildCommand));
            constructStyle = constructStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Construct the Build")));
            construct.m_6270_(constructStyle);
            source.m_81354_((Component)Component.m_237110_((String)"commands.construct.all-materials", (Object[])new Object[]{construct}), true);
        } else if (hasAnyRequiredMaterials.booleanValue()) {
            construct = Component.m_237113_((String)"Construct Anyways");
            constructStyle = Style.f_131099_;
            constructStyle = constructStyle.m_131157_(ChatFormatting.LIGHT_PURPLE);
            constructStyle = constructStyle.m_131142_(new ClickEvent(ClickEvent.Action.RUN_COMMAND, constructBuildCommand));
            constructStyle = constructStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237115_((String)"Construct the Build")));
            construct.m_6270_(constructStyle);
            source.m_81354_((Component)Component.m_237110_((String)"commands.construct.missing-materials", (Object[])new Object[]{construct}), true);
        }
        return 1;
    }
}

