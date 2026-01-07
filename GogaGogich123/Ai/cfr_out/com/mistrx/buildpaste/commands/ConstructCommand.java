/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.chat.CommonChatMessages;
import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mistrx.buildpaste.firebase.BuildDataStore;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mistrx.buildpaste.player.PlayerFunctions;
import com.mistrx.buildpaste.util.Functions;
import com.mistrx.buildpaste.util.Variables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3d;

public class ConstructCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"construct").executes(source -> ConstructCommand.constructCommand((CommandSourceStack)source.getSource(), null))).then(Commands.argument((String)"first argument", (ArgumentType)StringArgumentType.string()).executes(source -> ConstructCommand.constructCommand((CommandSourceStack)source.getSource(), StringArgumentType.getString((CommandContext)source, (String)"first argument")))));
    }

    public static int constructCommand(CommandSourceStack source, String firstarg) {
        BuildDataStore buildData;
        String buildId;
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        }
        catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        if (PlayerFunctions.hasInvalidUUID((Player)player).booleanValue()) {
            source.sendFailure((Component)Component.translatable((String)"util.doesnt-have-valid-uuid"));
        }
        BuildPasteMod.LOGGER.info("firstarg: " + firstarg);
        if (firstarg != null && firstarg.equals("constructbuild")) {
            String direction = Functions.getPlayerStringLookDirection((Player)player);
            Vector3d pos = new Vector3d(Math.floor(source.getPosition().x), Math.floor(source.getPosition().y), Math.floor(source.getPosition().z));
            try {
                Functions.pasteBuild((Player)player, Firebase.getLocallyStoredBuildData(PlayerDataManager.getOrCreatePlayerData((Player)player).getSelectedBuildId()), pos, direction, "dontplaceair", true);
            }
            catch (Exception e) {
                CommonChatMessages.handleCommonError((Player)player, e.getMessage());
                return 0;
            }
            source.sendSuccess(() -> Component.translatable((String)"commands.construct.success"), true);
            return 1;
        }
        Variables.playerItems = new HashMap();
        Variables.buildBlocks = new HashMap();
        NonNullList inventory = player.getInventory().getNonEquipmentItems();
        int inventorySize = inventory.size();
        for (int i = 0; i < inventorySize; ++i) {
            String[] itemArray = ((ItemStack)inventory.get(i)).toString().split(" ");
            Object itemName = itemArray[1].startsWith("minecraft:") ? itemArray[1] : "minecraft:" + itemArray[1];
            Integer itemAmount = Integer.parseInt(itemArray[0]);
            Variables.playerItems.merge((String)itemName, itemAmount, Integer::sum);
        }
        BuildPasteMod.LOGGER.info("Construct Command executed");
        try {
            buildId = Firebase.getSelectedBuildingID((Player)player);
        }
        catch (Exception e) {
            CommonChatMessages.handleCommonError((Player)player, e.getMessage());
            return 0;
        }
        try {
            buildData = Firebase.getBuildData(buildId);
        }
        catch (Exception e) {
            CommonChatMessages.handleCommonError((Player)player, e.getMessage());
            return 0;
        }
        for (int i = 0; i < buildData.getBlockIds().size(); ++i) {
            String block = Functions.getItemFromBlock(Functions.getBlockByIdOrName(buildData.getBlockIds().get(i)));
            Variables.buildBlocks.merge(block, 1, Integer::sum);
        }
        Variables.buildBlocks.remove("minecraft:air");
        Variables.playerItems.remove("minecraft:air");
        boolean hasAllRequiredMaterials = true;
        boolean hasAnyRequiredMaterials = false;
        StringBuilder requiredMaterials = new StringBuilder("Needed Materials:\n");
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
                    endText = " (" + String.valueOf(Variables.playerItems.get(key)) + "/" + amount.toString() + ")";
                    hasAllRequiredMaterials = false;
                }
            } else {
                hasAllRequiredMaterials = false;
            }
            requiredMaterials.append(color).append(amount.toString()).append(" x ").append(key.replace("minecraft:", "")).append((String)endText).append("\n");
        }
        String constructBuildCommand = "/construct constructbuild";
        String finalRequiredMaterials = requiredMaterials.toString();
        source.sendSuccess(() -> Component.literal((String)finalRequiredMaterials), true);
        if (hasAllRequiredMaterials) {
            construct = SimpleComponent.text("Construct", ChatFormatting.LIGHT_PURPLE).clickRunCommand(constructBuildCommand).hoverText("Construct the build");
            source.sendSuccess(() -> Component.translatable((String)"commands.construct.all-materials", (Object[])new Object[]{construct}), true);
        } else if (hasAnyRequiredMaterials) {
            construct = SimpleComponent.text("Construct Anyways", ChatFormatting.LIGHT_PURPLE).clickRunCommand(constructBuildCommand).hoverText("Construct the build");
            source.sendSuccess(() -> Component.translatable((String)"commands.construct.missing-materials", (Object[])new Object[]{construct}), true);
        }
        return 1;
    }
}

