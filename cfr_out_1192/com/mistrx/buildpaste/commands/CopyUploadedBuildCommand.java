/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.util.Variables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class CopyUploadedBuildCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"_copybuildid").requires(source -> source.m_6761_(4))).executes(source -> CopyUploadedBuildCommand.copyBuildId((CommandSourceStack)source.getSource())));
    }

    private static Integer copyBuildId(CommandSourceStack source) {
        Integer response = 1;
        try {
            response = Firebase.setSelectedBuildingID(source.m_81375_().m_20149_());
        }
        catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        if (response == 1) {
            source.m_81354_((Component)Component.m_237115_((String)"commands.copyuploadedbuildid.success"), true);
        } else {
            MutableComponent buildpastecomponent = Component.m_237113_((String)"Buildpaste");
            Style componentStyle = Style.f_131099_;
            componentStyle = componentStyle.m_131157_(ChatFormatting.GREEN);
            componentStyle = componentStyle.m_131142_(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://buildpaste.net"));
            componentStyle = componentStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Visit Buildpaste.net (and maybe create an account...)").m_6270_(Style.f_131099_.m_131157_(ChatFormatting.GREEN))));
            buildpastecomponent.m_6270_(componentStyle);
            MutableComponent component = Component.m_237113_((String)"Copy To Clipboard");
            componentStyle = Style.f_131099_;
            componentStyle = componentStyle.m_131157_(ChatFormatting.AQUA);
            componentStyle = componentStyle.m_131142_(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/paste " + Variables.uploadedBuildID));
            componentStyle = componentStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Copy build to Clipboard").m_6270_(Style.f_131099_.m_131157_(ChatFormatting.AQUA))));
            component.m_6270_(componentStyle);
            source.m_81354_((Component)Component.m_237110_((String)"commands.copyuploadedbuildid.error", (Object[])new Object[]{buildpastecomponent, component}), true);
        }
        return 1;
    }
}

