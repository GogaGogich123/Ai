/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.events;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.commands.BuildPasteHelpCommand;
import com.mistrx.buildpaste.commands.ConstructCommand;
import com.mistrx.buildpaste.commands.CopyUploadedBuildCommand;
import com.mistrx.buildpaste.commands.DisconnectAccountCommand;
import com.mistrx.buildpaste.commands.OpenAfterPasteMenuCommand;
import com.mistrx.buildpaste.commands.PasteCommand;
import com.mistrx.buildpaste.commands.PositionSelectorCommand;
import com.mistrx.buildpaste.commands.PrintCommand;
import com.mistrx.buildpaste.commands.RemovePosCommand;
import com.mistrx.buildpaste.commands.SetFirstPositionCommand;
import com.mistrx.buildpaste.commands.SetSecondPositionCommand;
import com.mistrx.buildpaste.commands.UndoPasteCommand;
import com.mistrx.buildpaste.commands.UploadCommand;
import com.mistrx.buildpaste.commands.VerifyCommand;
import com.mistrx.buildpaste.commands._ClearCommand;
import com.mistrx.buildpaste.commands._FillCommandNoRestriction;
import com.mistrx.buildpaste.commands._SetBlockCommand;
import com.mistrx.buildpaste.commands._TitleCommand;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.gui.PasteMenuHandler;
import com.mistrx.buildpaste.util.BuildExamples;
import com.mistrx.buildpaste.util.Functions;
import com.mistrx.buildpaste.util.RegistryHandler;
import com.mistrx.buildpaste.util.Variables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.math.Vector3d;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="buildpaste", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class ModClientEvents {
    public static Vector3d lastHitPosition;
    public static Vector3d pos1;
    public static Vector3d pos2;
    private static boolean hasJoinedWorldOnce;
    private static boolean clearCarriedItemNextTime;
    private static Integer tickCount;

    public static MinecartChest SetItemsInMinecart(MinecartChest minecart) {
        return null;
    }

    @SubscribeEvent
    public static void registerCommandsEvent(RegisterCommandsEvent event) {
        BuildPasteHelpCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        UploadCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        PasteCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        VerifyCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        DisconnectAccountCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        UndoPasteCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        SetFirstPositionCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        SetSecondPositionCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        RemovePosCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        PositionSelectorCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        OpenAfterPasteMenuCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        ConstructCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        PrintCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        CommandBuildContext commandbuildcontext = new CommandBuildContext((RegistryAccess)RegistryAccess.f_123049_.get());
        commandbuildcontext.m_227135_(CommandBuildContext.MissingTagAccessPolicy.RETURN_EMPTY);
        _TitleCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        _SetBlockCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher(), commandbuildcontext);
        _ClearCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher(), commandbuildcontext);
        _FillCommandNoRestriction.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher(), commandbuildcontext);
        CopyUploadedBuildCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        BuildPasteMod.LOGGER.info("Minecraft Started Event");
        Functions.setItemEqualsBlockHashmap();
    }

    @SubscribeEvent
    public static void onLeftClickWithPositionSelector(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() == null) {
            return;
        }
        Player player = event.getEntity();
        Level world = player.m_20193_();
        if (player.m_21120_(InteractionHand.MAIN_HAND).m_41720_() == RegistryHandler.POSITION_SELECTOR.get()) {
            BlockPos hitPos = event.getPos();
            pos1 = new Vector3d((double)hitPos.m_123341_(), (double)hitPos.m_123342_(), (double)hitPos.m_123343_());
            if (world.f_46443_) {
                BuildPasteMod.LOGGER.info("Client side");
                String posString = "(" + Math.round(hitPos.m_123341_()) + ", " + Math.round(hitPos.m_123342_()) + ", " + Math.round(hitPos.m_123343_()) + ")";
                player.m_213846_((Component)Component.m_237110_((String)"item.buildpaste.position_selecter.pos1", (Object[])new Object[]{posString}));
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEvent(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (event.getEntity() == null) {
            return;
        }
        Player player = event.getEntity();
        Level world = player.m_20193_();
        if (player.m_21120_(InteractionHand.MAIN_HAND).m_41720_() == RegistryHandler.POSITION_SELECTOR.get()) {
            BlockPos hitPos = event.getPos();
            pos2 = new Vector3d((double)hitPos.m_123341_(), (double)hitPos.m_123342_(), (double)hitPos.m_123343_());
            if (world.f_46443_) {
                String posString = "(" + Math.round(hitPos.m_123341_()) + ", " + Math.round(hitPos.m_123342_()) + ", " + Math.round(hitPos.m_123343_()) + ")";
                player.m_213846_((Component)Component.m_237110_((String)"item.buildpaste.position_selecter.pos2", (Object[])new Object[]{posString}));
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void ContainerEvent(PlayerContainerEvent event) {
        if (!PasteMenuHandler.minecartIsBeingOpened) {
            if (PasteMenuHandler.minecart == null) {
                return;
            }
            PasteMenuHandler.isOpened = false;
            PasteMenuHandler.currentMenu = "";
            PasteMenuHandler.minecart.m_6211_();
            PasteMenuHandler.minecart.m_6074_();
            BuildPasteMod.LOGGER.info("Close Container");
            return;
        }
        PasteMenuHandler.minecartIsBeingOpened = false;
        PasteMenuHandler.isOpened = true;
        BuildPasteMod.LOGGER.info("Open Container");
        PasteMenuHandler.setPreviousMinecart();
    }

    @SubscribeEvent
    public static void WorldEnterEvent(PlayerEvent.PlayerLoggedInEvent event) {
        BuildPasteMod.LOGGER.info("PlayerLoggedInEvent");
        if (event.getEntity() == null) {
            return;
        }
        final Player player = event.getEntity();
        if (player.m_9236_().f_46443_) {
            return;
        }
        if (Objects.equals(Variables.uploadedBuildID, "")) {
            if (hasJoinedWorldOnce) {
                return;
            }
            String[] randomBuildArray = BuildExamples.getRandomBuild();
            String id = randomBuildArray[0];
            String name = randomBuildArray[1];
            MutableComponent paste = Component.m_237113_((String)name);
            Style pasteStyle = Style.f_131099_;
            pasteStyle = pasteStyle.m_131157_(ChatFormatting.LIGHT_PURPLE);
            pasteStyle = pasteStyle.m_131142_(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/paste " + id));
            pasteStyle = pasteStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)("Paste " + name + " into your world")).m_130940_(ChatFormatting.LIGHT_PURPLE)));
            paste.m_6270_(pasteStyle);
            MutableComponent buildpaste = Component.m_237113_((String)"Buildpaste");
            Style buildpasteStyle = Style.f_131099_;
            buildpasteStyle = buildpasteStyle.m_131157_(ChatFormatting.UNDERLINE);
            buildpasteStyle = buildpasteStyle.m_131142_(new ClickEvent(ClickEvent.Action.OPEN_URL, Variables.url + "/category.html"));
            buildpasteStyle = buildpasteStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Visit Buildpaste.net").m_6270_(Style.f_131099_.m_131157_(ChatFormatting.GREEN))));
            buildpaste.m_6270_(buildpasteStyle);
            event.getEntity().m_213846_((Component)Component.m_237110_((String)"world.join.normal", (Object[])new Object[]{buildpaste, paste}));
            new Timer().schedule(new TimerTask(){

                @Override
                public void run() {
                    String mcuuid = player.m_20149_();
                    String memberLevel = Firebase.getPlayerMemberLevel(mcuuid);
                }
            }, 1L);
            hasJoinedWorldOnce = true;
        } else {
            MutableComponent paste = Component.m_237113_((String)"Paste");
            Style pasteStyle = Style.f_131099_;
            pasteStyle = pasteStyle.m_131157_(ChatFormatting.LIGHT_PURPLE);
            pasteStyle = pasteStyle.m_131142_(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/paste " + Variables.uploadedBuildID));
            pasteStyle = pasteStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Paste your Build").m_130940_(ChatFormatting.LIGHT_PURPLE)));
            paste.m_6270_(pasteStyle);
            player.m_213846_((Component)Component.m_237110_((String)"world.join.copied_build", (Object[])new Object[]{paste}));
        }
    }

    @SubscribeEvent
    public static void TickEvent(TickEvent.PlayerTickEvent event) throws IOException {
        Player player = event.player;
        if (tickCount % 20 * 3 == 0) {
            if (PasteMenuHandler.minecart != null && !PasteMenuHandler.clickDetected) {
                BuildPasteMod.LOGGER.info("  -  looking for item change");
                PasteMenuHandler.currentMinecartInventory.clear();
                int containerSize = PasteMenuHandler.minecart.m_6643_();
                for (int i = 0; i < containerSize; ++i) {
                    PasteMenuHandler.currentMinecartInventory.add(PasteMenuHandler.minecart.m_8020_(i));
                }
                if (!PasteMenuHandler.currentMinecartInventory.equals(PasteMenuHandler.previousMinecartInventory) && PasteMenuHandler.previousMinecartInventory.size() > 0) {
                    PasteMenuHandler.clickDetected = true;
                    int changedItemIndex = -1;
                    int size = PasteMenuHandler.currentMinecartInventory.size();
                    for (int i = 0; i < size; ++i) {
                        if (PasteMenuHandler.currentMinecartInventory.get(i).equals(PasteMenuHandler.previousMinecartInventory.get(i))) continue;
                        changedItemIndex = i;
                        break;
                    }
                    String color = "";
                    if (PasteMenuHandler.previousMinecartInventory.get(changedItemIndex).m_41786_().m_7383_().m_131135_() != null) {
                        color = Objects.requireNonNull(PasteMenuHandler.previousMinecartInventory.get(changedItemIndex).m_41786_().m_7383_().m_131135_()).toString();
                    }
                    PasteMenuHandler.handleClick(player, PasteMenuHandler.previousMinecartItemNames.get(changedItemIndex), PasteMenuHandler.previousMinecartInventory.get(changedItemIndex).m_41786_().getString(), color);
                    clearCarriedItemNextTime = true;
                }
            }
            if (pos1 != null && pos2 != null) {
                player.m_5661_((Component)Component.m_237113_((String)"Look at the front of your selected build and type /upload").m_6270_(Style.f_131099_.m_131140_(ChatFormatting.GREEN)), true);
            }
        }
        if (clearCarriedItemNextTime && !player.f_19853_.m_5776_()) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            BuildPasteMod.LOGGER.info("Clearing the carried item");
            serverPlayer.f_36095_.m_142503_(ItemStack.f_41583_);
            serverPlayer.f_36096_.m_142503_(ItemStack.f_41583_);
            serverPlayer.f_36096_.m_38946_();
            serverPlayer.f_36095_.m_6199_((Container)player.m_150109_());
            clearCarriedItemNextTime = false;
        }
        Integer n = tickCount;
        tickCount = tickCount + 1;
    }

    static {
        hasJoinedWorldOnce = false;
        clearCarriedItemNextTime = false;
        tickCount = 0;
    }
}

