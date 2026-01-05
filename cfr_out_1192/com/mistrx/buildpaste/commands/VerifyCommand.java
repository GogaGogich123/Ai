/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.commands;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.util.Variables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class VerifyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"connectaccounts").requires(source -> source.m_6761_(4))).executes(source -> VerifyCommand.verifyCommand((CommandSourceStack)source.getSource(), ""))).then(Commands.m_82129_((String)"email", (ArgumentType)MessageArgument.m_96832_()).executes(source -> VerifyCommand.verifyCommand((CommandSourceStack)source.getSource(), MessageArgument.m_96835_((CommandContext)source, (String)"email").getString()))));
    }

    public static int verifyCommand(CommandSourceStack source, String email) {
        if (!email.contains("@") && !email.equals("")) {
            source.m_81354_((Component)Component.m_237110_((String)"commands.verify.noemail", (Object[])new Object[]{email}), false);
            return 1;
        }
        if (email.equals("")) {
            source.m_81354_((Component)Component.m_237115_((String)"commands.verify.help"), false);
        } else {
            try {
                Variables.mcname = source.m_81375_().m_7755_().getString();
                Variables.uuid = source.m_81375_().m_20149_();
            }
            catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            BuildPasteMod.LOGGER.info("MC: " + Variables.mcname + ", " + Variables.uuid);
            Integer responseCode = Firebase.connectAccounts(email);
            if (responseCode == 200) {
                source.m_81354_((Component)Component.m_237110_((String)"commands.verify.verified", (Object[])new Object[]{email}), false);
            } else if (responseCode == 404) {
                MutableComponent buildpaste = Component.m_237113_((String)"Buildpaste.net");
                Style buildpasteStyle = Style.f_131099_;
                buildpasteStyle = buildpasteStyle.m_131157_(ChatFormatting.GREEN);
                buildpasteStyle = buildpasteStyle.m_131142_(new ClickEvent(ClickEvent.Action.OPEN_URL, Variables.url));
                buildpasteStyle = buildpasteStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Visit Buildpaste.net").m_6270_(Style.f_131099_.m_131157_(ChatFormatting.GREEN))));
                buildpaste.m_6270_(buildpasteStyle);
                MutableComponent profile = Component.m_237113_((String)"Profile");
                Style profileStyle = Style.f_131099_;
                profileStyle = profileStyle.m_131157_(ChatFormatting.GREEN);
                profileStyle = profileStyle.m_131142_(new ClickEvent(ClickEvent.Action.OPEN_URL, Variables.url + "/profileedit.html"));
                profileStyle = profileStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Edit your Profile").m_6270_(Style.f_131099_.m_131157_(ChatFormatting.GREEN))));
                profile.m_6270_(profileStyle);
                source.m_81354_((Component)Component.m_237110_((String)"commands.verify.emailnotfound", (Object[])new Object[]{email, Variables.mcname, buildpaste, profile}), false);
            } else {
                source.m_81354_((Component)Component.m_237115_((String)"commands.verify.error"), false);
            }
        }
        return 1;
    }
}

