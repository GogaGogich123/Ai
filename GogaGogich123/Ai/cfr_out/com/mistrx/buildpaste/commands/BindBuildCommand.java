/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.chat.CommonChatMessages;
import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.items.build_placer.BuildPlacerHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BindBuildCommand {
    public static HashMap<String, String> loadedBuildNames = new HashMap();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"bindbuild").executes(source -> BindBuildCommand.run((CommandSourceStack)source.getSource())));
    }

    public static Integer run(CommandSourceStack source) throws CommandSyntaxException {
        String selectedBuildId;
        ServerPlayer player = source.getPlayerOrException();
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!itemStack.getItem().equals(BuildPasteMod.BUILD_PLACER.get())) {
            source.sendFailure((Component)Component.translatable((String)"commands.bindbuild.wrong-item"));
            return 0;
        }
        try {
            selectedBuildId = Firebase.getSelectedBuildingID((Player)player);
        }
        catch (Exception e) {
            if (e.getMessage().equals("getbuildid/no-account")) {
                SimpleComponent.TextBuilder component = SimpleComponent.text("Learn More", ChatFormatting.LIGHT_PURPLE).clickOpenUrl("https://www.youtube.com/watch?v=3p3VZ0WffQI").hoverText("Watch a short YouTube video to get started");
                source.sendFailure((Component)Component.translatable((String)"commands.bindbuild.error", (Object[])new Object[]{component}));
                return 0;
            }
            CommonChatMessages.handleCommonError((Player)player, e.getMessage());
            return 0;
        }
        BuildPlacerHandler.setTag((Player)player, "buildpaste.bound-build-id", selectedBuildId);
        String name = "Unnamed";
        if (!loadedBuildNames.containsKey(selectedBuildId)) {
            String buildName = Firebase.getBuildName(selectedBuildId);
            if (buildName != null) {
                name = buildName;
            }
        } else {
            name = loadedBuildNames.get(selectedBuildId);
        }
        loadedBuildNames.put(selectedBuildId, name);
        player.getItemInHand(InteractionHand.MAIN_HAND).set(DataComponents.CUSTOM_NAME, (Object)Component.literal((String)name));
        String finalName = name;
        source.sendSuccess(() -> Component.translatable((String)"commands.bindbuild.success", (Object[])new Object[]{finalName}), false);
        return 1;
    }
}

