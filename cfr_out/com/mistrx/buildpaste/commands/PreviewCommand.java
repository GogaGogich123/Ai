/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.chat.CommonChatMessages;
import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mistrx.buildpaste.firebase.BuildDataStore;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mistrx.buildpaste.rendering.RenderHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PreviewCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"preview").executes(source -> PreviewCommand.preview((CommandSourceStack)source.getSource())));
    }

    public static int preview(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        new Timer().schedule(new TimerTask((Player)player){
            final /* synthetic */ Player val$player;
            {
                this.val$player = player;
            }

            @Override
            public void run() {
                String oldBuildID = PlayerDataManager.getOrCreatePlayerData(this.val$player).getSelectedBuildId();
                String buildId = null;
                try {
                    buildId = Firebase.getSelectedBuildingID(this.val$player);
                }
                catch (Exception e) {
                    if (e.getMessage().equals("getbuildid/no-account")) {
                        SimpleComponent.TextBuilder component = SimpleComponent.text("Buildpaste.net", ChatFormatting.GREEN).clickOpenUrl("https://buildpaste.net/category?&player=" + Objects.requireNonNull(this.val$player.getDisplayName()).getString()).hoverText("Open buildpaste.net and create an account");
                        this.val$player.displayClientMessage((Component)Component.translatable((String)"account.required", (Object[])new Object[]{component}), false);
                        return;
                    }
                    throw new RuntimeException(e);
                }
                try {
                    BuildDataStore buildDataStore = Firebase.getBuildData(buildId);
                }
                catch (Exception e) {
                    CommonChatMessages.handleCommonError(this.val$player, e.getMessage());
                    return;
                }
                RenderHandler.startPreviewPosition = this.val$player.blockPosition();
                RenderHandler.isUsingPreviewCommand = true;
                RenderHandler.prepareRenderBlocks(this.val$player);
                SimpleComponent.TextBuilder paste = SimpleComponent.text("Paste", ChatFormatting.LIGHT_PURPLE).clickRunCommand("/_pastebuildplacer").hoverText("Paste the build where the preview is shown", ChatFormatting.LIGHT_PURPLE);
                SimpleComponent.TextBuilder remove = SimpleComponent.text("Remove Preview", ChatFormatting.RED).clickRunCommand("/_removerender").hoverText("Remove the preview");
                this.val$player.displayClientMessage((Component)Component.translatable((String)"commands.preview.success", (Object[])new Object[]{paste, remove}), false);
            }
        }, 1L);
        return 1;
    }
}

