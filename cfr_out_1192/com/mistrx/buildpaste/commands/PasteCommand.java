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
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

public final class PasteCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"paste").requires(source -> source.m_6761_(4))).executes(source -> PasteCommand.pasteCommand((CommandSourceStack)source.getSource(), null, null))).then(Commands.m_82129_((String)"first argument", (ArgumentType)StringArgumentType.string()).executes(source -> PasteCommand.pasteCommand((CommandSourceStack)source.getSource(), StringArgumentType.getString((CommandContext)source, (String)"first argument"), null)))).then(Commands.m_82129_((String)"first argument", (ArgumentType)StringArgumentType.string()).then(Commands.m_82129_((String)"second argument", (ArgumentType)StringArgumentType.string()).executes(source -> PasteCommand.pasteCommand((CommandSourceStack)source.getSource(), StringArgumentType.getString((CommandContext)source, (String)"first argument"), StringArgumentType.getString((CommandContext)source, (String)"second argument"))))));
    }

    public static int pasteCommand(CommandSourceStack source, String firstarg, String secondarg) {
        String[] args = new String[]{firstarg, secondarg};
        BuildPasteMod.LOGGER.info("Paste Command executed");
        String direction = null;
        try {
            float yRot = source.m_81375_().m_5675_(1.0f);
            direction = Functions.getLookDirection(yRot);
            Functions.setPlayerVariables((Player)source.m_81375_());
            if (Functions.sendInvalidUUIDMessage((Player)source.m_81375_()).booleanValue()) {
                source.m_81354_((Component)Component.m_237115_((String)"util.doesnt-have-valid-uuid"), true);
            }
        }
        catch (CommandSyntaxException e) {
            BuildPasteMod.LOGGER.info("Error in try catch at pasteCommand");
            e.printStackTrace();
        }
        BuildPasteMod.LOGGER.info("Direction: " + direction);
        String response = Functions.setBuilding(new Vector3d(Math.floor(source.m_81371_().f_82479_), Math.floor(source.m_81371_().f_82480_), Math.floor(source.m_81371_().f_82481_)), direction, args, true);
        BuildPasteMod.LOGGER.info("getBuild Response: " + response);
        if (response != null) {
            if (response.equals("success")) {
                Integer blocksCount = (int)(Firebase.size.f_86214_ * Firebase.size.f_86215_ * Firebase.size.f_86216_);
                String firstPos = Functions.PosToString(Variables.lastFirstPos);
                String secondPos = Functions.PosToString(Variables.lastSecondPos);
                MutableComponent modify = Component.m_237113_((String)"[Quick Actions]");
                Style publishStyle = Style.f_131099_;
                publishStyle = publishStyle.m_131157_(ChatFormatting.GOLD);
                publishStyle = publishStyle.m_131142_(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/_openpastemenu " + firstPos + " " + secondPos + " " + Variables.buildID));
                publishStyle = publishStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Change Blocks, Materials, Undo or turn your build into a real-life 3D printed replica").m_130940_(ChatFormatting.GOLD)));
                modify.m_6270_(publishStyle);
                if (!Objects.equals(Variables.memberLevel, "free")) {
                    MutableComponent incompatibleBlocksComponent = Component.m_237113_((String)(Variables.incompatibleBlocksAmount != 0 && Variables.incompatibleBlocksExampleBlocksReplacedArray.size() != 0 ? " (" + Variables.incompatibleBlocksAmount + " incompatible blocks replaced with similar ones)" : ""));
                    Object exampleBlockString = "";
                    int blockAmountInExampleBlocks = 0;
                    for (int i = 0; i < Variables.incompatibleBlocksExampleBlocksReplacedArray.size(); ++i) {
                        String exampleBlock = Variables.incompatibleBlocksExampleBlocksReplacedArray.get(i);
                        String[] exampleBlockSplit = exampleBlock.split(",");
                        if (exampleBlockSplit.length != 3) continue;
                        try {
                            blockAmountInExampleBlocks += Integer.parseInt(exampleBlockSplit[2]);
                        }
                        catch (NumberFormatException numberFormatException) {
                            // empty catch block
                        }
                        exampleBlockString = (String)exampleBlockString + ChatFormatting.WHITE + exampleBlockSplit[2] + "x " + ChatFormatting.RED + exampleBlockSplit[0] + ChatFormatting.WHITE + " -> " + ChatFormatting.GREEN + exampleBlockSplit[1] + (i != Variables.incompatibleBlocksExampleBlocksReplacedArray.size() - 1 ? "\n" : "");
                        if (i != Variables.incompatibleBlocksExampleBlocksReplacedArray.size() - 1 || blockAmountInExampleBlocks >= Variables.incompatibleBlocksAmount) continue;
                        exampleBlockString = (String)exampleBlockString + "\n" + ChatFormatting.WHITE + ChatFormatting.ITALIC + "And " + (Variables.incompatibleBlocksAmount - blockAmountInExampleBlocks) + " more blocks...";
                    }
                    Style incompatibleBlocksStyle = Style.f_131099_.m_131155_(Boolean.valueOf(true));
                    incompatibleBlocksStyle = incompatibleBlocksStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)exampleBlockString)));
                    incompatibleBlocksComponent.m_6270_(incompatibleBlocksStyle);
                    source.m_81354_((Component)Component.m_237110_((String)"commands.paste.success", (Object[])new Object[]{String.valueOf(blocksCount), incompatibleBlocksComponent, modify}), true);
                } else {
                    MutableComponent incompatibleBlocksComponent = Component.m_237113_((String)(Functions.newerBlocksAmountFound + " incompatible blocks found"));
                    Style incompatibleBlocksStyle = Style.f_131099_;
                    incompatibleBlocksStyle = incompatibleBlocksStyle.m_131157_(ChatFormatting.UNDERLINE);
                    incompatibleBlocksStyle = incompatibleBlocksStyle.m_131142_(new ClickEvent(ClickEvent.Action.OPEN_URL, Variables.url + "/premium?r=backwards_compatibility"));
                    incompatibleBlocksStyle = incompatibleBlocksStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"").m_7220_((Component)Component.m_237113_((String)(Functions.newerBlocksAmountFound + " blocks in this build are from a newer Minecraft version and couldn't be pasted. ")).m_130940_(ChatFormatting.RESET)).m_7220_((Component)Component.m_237113_((String)"\n\nGet Buildpaste Plus to automatically replace these blocks with similar ones available in your version, ensuring compatibility and preserving the builds' integrity.").m_130940_(ChatFormatting.BOLD)).m_7220_((Component)Component.m_237113_((String)"\n\n[Click to Learn More]").m_130940_(ChatFormatting.GOLD))));
                    incompatibleBlocksComponent.m_6270_(incompatibleBlocksStyle);
                    source.m_81354_((Component)Component.m_237110_((String)"commands.paste.success", (Object[])new Object[]{String.valueOf(blocksCount), Functions.newerBlocksAmountFound != 0 ? Component.m_237113_((String)" (").m_7220_((Component)incompatibleBlocksComponent).m_130946_(")") : "", modify}), true);
                }
            } else if (response.equals("error")) {
                String url = Variables.url;
                MutableComponent buildpastecomponent = Component.m_237113_((String)"Buildpaste.net");
                Style componentStyle = Style.f_131099_;
                componentStyle = componentStyle.m_131157_(ChatFormatting.GREEN);
                componentStyle = componentStyle.m_131142_(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                componentStyle = componentStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Visit Buildpaste.net").m_6270_(Style.f_131099_.m_131157_(ChatFormatting.GREEN))));
                buildpastecomponent.m_6270_(componentStyle);
                MutableComponent pastecommandcomponent = Component.m_237113_((String)"Click me!");
                componentStyle = Style.f_131099_;
                componentStyle = componentStyle.m_131157_(ChatFormatting.LIGHT_PURPLE);
                componentStyle = componentStyle.m_131142_(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/paste Mi2QSVIFOiJaNBRZS5Xm"));
                componentStyle = componentStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Paste an example build").m_6270_(Style.f_131099_.m_131157_(ChatFormatting.LIGHT_PURPLE))));
                pastecommandcomponent.m_6270_(componentStyle);
                source.m_81354_((Component)Component.m_237110_((String)"commands.paste.error", (Object[])new Object[]{buildpastecomponent, pastecommandcomponent}), true);
            } else if (response.equals("error-404")) {
                source.m_81352_((Component)Component.m_237115_((String)"commands.paste.error-404"));
            } else if (response.equals("random-error")) {
                source.m_81352_((Component)Component.m_237115_((String)"commands.paste.random-error"));
            }
        }
        return 1;
    }
}

