/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.events;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.Config;
import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mistrx.buildpaste.commands.BindBuildCommand;
import com.mistrx.buildpaste.commands.BuildPasteHelpCommand;
import com.mistrx.buildpaste.commands.BuildPlacerCommand;
import com.mistrx.buildpaste.commands.ConnectAccountsCommand;
import com.mistrx.buildpaste.commands.ConstructCommand;
import com.mistrx.buildpaste.commands.CopyUploadedBuildCommand;
import com.mistrx.buildpaste.commands.DisconnectAccountCommand;
import com.mistrx.buildpaste.commands.PasteCommand;
import com.mistrx.buildpaste.commands.PasteForBuildPlacerCommand;
import com.mistrx.buildpaste.commands.PasteHistoryCommand;
import com.mistrx.buildpaste.commands.PositionSelectorCommand;
import com.mistrx.buildpaste.commands.PreviewCommand;
import com.mistrx.buildpaste.commands.RemovePosCommand;
import com.mistrx.buildpaste.commands.SetFirstPositionCommand;
import com.mistrx.buildpaste.commands.SetSecondPositionCommand;
import com.mistrx.buildpaste.commands.UndoPasteCommand;
import com.mistrx.buildpaste.commands.UploadCommand;
import com.mistrx.buildpaste.commands._ClearCommand;
import com.mistrx.buildpaste.commands._FillCommandNoRestriction;
import com.mistrx.buildpaste.commands._OpenAfterPasteMenuCommand;
import com.mistrx.buildpaste.commands._OpenBuildPlacerMenu;
import com.mistrx.buildpaste.commands._RemoveRenderCommand;
import com.mistrx.buildpaste.commands._SetBlockCommand;
import com.mistrx.buildpaste.firebase.Firebase;
import com.mistrx.buildpaste.gui.PasteMenuHandler;
import com.mistrx.buildpaste.items.build_placer.BuildPlacerHandler;
import com.mistrx.buildpaste.items.position_selector.PositionSelectorHandler;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mistrx.buildpaste.player.UploadingDataStore;
import com.mistrx.buildpaste.rendering.Render;
import com.mistrx.buildpaste.rendering.RenderHandler;
import com.mistrx.buildpaste.util.CommandChainLengthHandler;
import com.mistrx.buildpaste.util.Functions;
import com.mistrx.buildpaste.util.Raycast;
import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3d;

@EventBusSubscriber(modid="buildpaste")
public class GameEvents {
    private static boolean hasJoinedWorldOnce = false;
    private static boolean hasBuildPlacerSelected = false;
    private static boolean clearCarriedItemNextTime = false;
    private static int lastSelectedItemSlot = 0;
    private static Integer tickCount = 0;
    private static boolean lastIsUploadingStatus = false;

    @SubscribeEvent
    public static void registerCommandsEvent(RegisterCommandsEvent event) {
        BuildPasteHelpCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        ConnectAccountsCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        DisconnectAccountCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        PasteCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        ConstructCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        PreviewCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        UndoPasteCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        BuildPlacerCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        BindBuildCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        UploadCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        CopyUploadedBuildCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        PositionSelectorCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        SetFirstPositionCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        SetSecondPositionCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        RemovePosCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        PasteHistoryCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        _OpenAfterPasteMenuCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        PasteForBuildPlacerCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        _SetBlockCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher(), event.getBuildContext());
        _ClearCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher(), event.getBuildContext());
        _FillCommandNoRestriction.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher(), event.getBuildContext());
        _RemoveRenderCommand.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        _OpenBuildPlacerMenu.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        Functions.setItemEqualsBlockHashmap();
    }

    @SubscribeEvent
    public static void WorldEnterEvent(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player player = (Player)entity;
        BuildPasteMod.LOGGER.info("PlayerLoggedInEvent");
        if (player.level().isClientSide) {
            return;
        }
        CommandChainLengthHandler.increaseMaxCommandChainLengthIfNecessary(player.getServer());
        if (!player.isCreative()) {
            return;
        }
        if (Objects.equals(PlayerDataManager.getOrCreatePlayerData(player).getUploadedBuildID(), "")) {
            if (hasJoinedWorldOnce) {
                return;
            }
            if (!Config.showEntryMessage) {
                return;
            }
            new Timer().schedule(new TimerTask(){

                @Override
                public void run() {
                    PlayerDataManager.getOrCreatePlayerData(player);
                    String buildId = null;
                    try {
                        buildId = Firebase.getSelectedBuildingID(player);
                        SimpleComponent.TextBuilder mcname = SimpleComponent.text(Objects.requireNonNull(player.getDisplayName()).getString());
                        SimpleComponent.TextBuilder paste = SimpleComponent.text("Paste", ChatFormatting.LIGHT_PURPLE).clickRunCommand("/paste").hoverText("Paste your currently selected build into your world", ChatFormatting.LIGHT_PURPLE);
                        SimpleComponent.TextBuilder buildplacer = SimpleComponent.text("Get Build Placer", ChatFormatting.GREEN).clickSuggestCommand("/buildplacer").hoverText("Get the Build Placer item for easily placing structures", ChatFormatting.GREEN);
                        player.displayClientMessage((Component)Component.translatable((String)"world.join.normal", (Object[])new Object[]{mcname, paste, buildplacer}), false);
                    }
                    catch (Exception e) {
                        SimpleComponent.TextBuilder paste = SimpleComponent.text("Get Started", ChatFormatting.LIGHT_PURPLE).clickOpenUrl("https://buildpaste.net/category?&player=" + Objects.requireNonNull(player.getDisplayName()).getString()).hoverText("Open buildpaste.net, create an account and select a build you like. That's it!", ChatFormatting.LIGHT_PURPLE);
                        SimpleComponent.TextBuilder buildplacer = SimpleComponent.text("Build Placer").clickRunCommand("/buildplacer").hoverText("Get the Build Placer item for easily placing structures", ChatFormatting.GREEN);
                        player.displayClientMessage((Component)Component.translatable((String)"world.join.get_started", (Object[])new Object[]{buildplacer, paste}), false);
                    }
                }
            }, 1L);
            hasJoinedWorldOnce = true;
        } else {
            SimpleComponent.TextBuilder paste = SimpleComponent.text("Paste", ChatFormatting.LIGHT_PURPLE).clickSuggestCommand("/paste " + PlayerDataManager.getOrCreatePlayerData(player).getUploadedBuildID()).hoverText("Paste your build", ChatFormatting.LIGHT_PURPLE);
            player.displayClientMessage((Component)Component.translatable((String)"world.join.copied_build", (Object[])new Object[]{paste}), false);
        }
        Config.FIRST_TIME_USING_BUILDPASTE.set((Object)((Integer)Config.FIRST_TIME_USING_BUILDPASTE.get()));
    }

    @SubscribeEvent
    public static void onWorldRenderLast(RenderLevelStageEvent.AfterTripwireBlocks event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (RenderHandler.shouldRenderStructure && (RenderHandler.hasRaycastHitSomething || RenderHandler.isUsingPreviewCommand)) {
            Render.renderBlocks((RenderLevelStageEvent)event);
        }
        Vector3d pos1 = PlayerDataManager.getOrCreatePlayerData((Player)player).getUploadingDataStore().getPos1();
        Vector3d pos2 = PlayerDataManager.getOrCreatePlayerData((Player)player).getUploadingDataStore().getPos1();
        if (pos1 != null && pos2 != null) {
            Render.renderSelectionOutline((RenderLevelStageEvent)event, (Player)player);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlockWithSpecialItemEvent(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (GameEvents.handleItemClick(event.getEntity(), event.getPos(), CLICK_TYPE.LEFT)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickAirWithSpecialItemEvent(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        GameEvents.handleItemClick(event.getEntity(), null, CLICK_TYPE.LEFT);
    }

    @SubscribeEvent
    public static void onRightClickBlockWithSpecialItemEvent(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (GameEvents.handleItemClick(event.getEntity(), event.getPos(), CLICK_TYPE.RIGHT)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickAirWithSpecialItemEvent(PlayerInteractEvent.RightClickEmpty event) {
        GameEvents.handleItemClick(event.getEntity(), null, CLICK_TYPE.RIGHT);
    }

    private static boolean handleItemClick(Player player, BlockPos hitPos, CLICK_TYPE clickType) {
        Level world = player.level();
        Item carriedItem = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        if (carriedItem == BuildPasteMod.POSITION_SELECTOR.get()) {
            if (world.isClientSide) {
                return false;
            }
            if (hitPos == null) {
                return false;
            }
            Vector3d hitPosVector = new Vector3d((double)hitPos.getX(), (double)hitPos.getY(), (double)hitPos.getZ());
            if (clickType == CLICK_TYPE.LEFT) {
                PlayerDataManager.getOrCreatePlayerData(player).getUploadingDataStore().setPos1(hitPosVector);
            } else if (clickType == CLICK_TYPE.RIGHT) {
                PlayerDataManager.getOrCreatePlayerData(player).getUploadingDataStore().setPos2(hitPosVector);
            }
            PositionSelectorHandler.sendPositionSelectedConfirmationMessage(player, hitPosVector, clickType == CLICK_TYPE.LEFT ? PositionSelectorHandler.WHAT_POSITON.POS1 : PositionSelectorHandler.WHAT_POSITON.POS2);
            return true;
        }
        if (carriedItem == BuildPasteMod.BUILD_PLACER.get()) {
            if (!world.isClientSide && hitPos == null) {
                return false;
            }
            if (world.isClientSide && hitPos != null) {
                return false;
            }
            if (clickType == CLICK_TYPE.LEFT) {
                BuildPlacerHandler.handleLeftClick(player);
            } else if (clickType == CLICK_TYPE.RIGHT) {
                BuildPlacerHandler.handleRightClick(player);
            }
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onTooltipHover(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        if (itemStack.getItem().equals(BuildPasteMod.BUILD_PLACER.get())) {
            if (itemStack.get(DataComponents.CUSTOM_DATA) == null) {
                event.getToolTip().add(Component.translatable((String)"tooltip.build_placer.default"));
                return;
            }
            List allKeys = ((CustomData)itemStack.get(DataComponents.CUSTOM_DATA)).copyTag().keySet().stream().toList();
            if (allKeys.isEmpty()) {
                return;
            }
            for (int i = 0; i < allKeys.size(); ++i) {
                String key = (String)allKeys.get(i);
                if (key.equals("buildpaste.bound-build-id")) continue;
                String value = (String)((CustomData)itemStack.get(DataComponents.CUSTOM_DATA)).copyTag().getString(key).get();
                MutableComponent newComponent = Component.translatable((String)("tags." + key));
                newComponent.append(": " + value);
                if (value.equals("true")) {
                    newComponent.withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN));
                } else {
                    newComponent.withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
                }
                event.getToolTip().add(newComponent);
            }
        } else if (itemStack.getItem().equals(BuildPasteMod.POSITION_SELECTOR.get())) {
            event.getToolTip().add(Component.translatable((String)"tooltip.position_selector.default"));
        } else {
            String menuActionHint;
            if (itemStack.get(DataComponents.CUSTOM_DATA) == null) {
                return;
            }
            CompoundTag compoundTag = ((CustomData)itemStack.get(DataComponents.CUSTOM_DATA)).copyTag();
            if (!compoundTag.contains("menu.action")) {
                return;
            }
            if (compoundTag.contains("menu.active")) {
                if (compoundTag.getString("menu.active").equals("true")) {
                    event.getToolTip().add(Component.literal((String)"Enabled").withStyle(ChatFormatting.GREEN));
                } else {
                    event.getToolTip().add(Component.literal((String)"Disabled").withStyle(ChatFormatting.DARK_GRAY));
                }
                event.getToolTip().add(Component.literal((String)""));
            }
            if (!Component.translatable((String)("menu.action." + (menuActionHint = (String)compoundTag.getString("menu.action").get()))).getString().isEmpty()) {
                event.getToolTip().add(Component.translatable((String)("menu.action." + menuActionHint)).withStyle(ChatFormatting.GRAY));
            }
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
            PasteMenuHandler.minecart.clearContent();
            Level level = event.getEntity().level();
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                PasteMenuHandler.minecart.kill(serverLevel);
            }
            BuildPasteMod.LOGGER.info("Close Container");
            return;
        }
        PasteMenuHandler.minecartIsBeingOpened = false;
        PasteMenuHandler.isOpened = true;
        BuildPasteMod.LOGGER.info("Open Container");
    }

    @SubscribeEvent
    public static void TickEvent(PlayerTickEvent.Pre event) throws IOException {
        Player player = event.getEntity();
        if (tickCount % 20 * 3 == 0) {
            UploadingDataStore playerUploadingDataStore;
            if (PasteMenuHandler.minecart != null && !PasteMenuHandler.clickDetected) {
                BuildPasteMod.LOGGER.info("  -  looking for item change");
                PasteMenuHandler.currentMinecartInventory.clear();
                int containerSize = PasteMenuHandler.minecart.getContainerSize();
                for (int i = 0; i < containerSize; ++i) {
                    PasteMenuHandler.currentMinecartInventory.add(PasteMenuHandler.minecart.getItem(i));
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
                    if (PasteMenuHandler.previousMinecartInventory.get(changedItemIndex).getHoverName().getStyle().getColor() != null) {
                        color = Objects.requireNonNull(PasteMenuHandler.previousMinecartInventory.get(changedItemIndex).getHoverName().getStyle().getColor()).toString();
                    }
                    String customTag = null;
                    if (PasteMenuHandler.previousMinecartInventory.get(changedItemIndex).get(DataComponents.CUSTOM_DATA) != null) {
                        customTag = (String)((CustomData)PasteMenuHandler.previousMinecartInventory.get(changedItemIndex).get(DataComponents.CUSTOM_DATA)).copyTag().getString("menu.action").get();
                    }
                    PasteMenuHandler.handleClick(player, PasteMenuHandler.lastMinecartContents.get(changedItemIndex));
                    clearCarriedItemNextTime = true;
                }
            }
            if ((playerUploadingDataStore = PlayerDataManager.getOrCreatePlayerData(player).getUploadingDataStore()).getPos1() != null && playerUploadingDataStore.getPos2() != null) {
                if (PositionSelectorHandler.isUploading) {
                    if (PositionSelectorHandler.uploadSize > 500000) {
                        player.displayClientMessage((Component)Component.translatable((String)"position_selector.uploading_big"), true);
                    } else {
                        player.displayClientMessage((Component)Component.translatable((String)"position_selector.uploading"), true);
                    }
                    lastIsUploadingStatus = true;
                } else {
                    player.displayClientMessage((Component)Component.translatable((String)"position_selector.upload_hint"), true);
                }
            } else if (lastIsUploadingStatus) {
                player.displayClientMessage((Component)Component.literal((String)""), true);
                lastIsUploadingStatus = false;
            }
        }
        if (clearCarriedItemNextTime && !player.level().isClientSide()) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            BuildPasteMod.LOGGER.info("Clearing the carried item");
            serverPlayer.inventoryMenu.setCarried(ItemStack.EMPTY);
            serverPlayer.containerMenu.setCarried(ItemStack.EMPTY);
            serverPlayer.containerMenu.broadcastChanges();
            serverPlayer.inventoryMenu.slotsChanged((Container)player.getInventory());
            clearCarriedItemNextTime = false;
            if (PasteMenuHandler.closeContainerNextTime) {
                player.closeContainer();
                PasteMenuHandler.closeContainerNextTime = false;
            }
        }
        if (player.level().isClientSide) {
            int currentItemSlot = player.getInventory().getSelectedSlot();
            if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == BuildPasteMod.BUILD_PLACER.get() && lastSelectedItemSlot == currentItemSlot) {
                if (Objects.equals(BuildPlacerHandler.status, "build-loaded")) {
                    BlockHitResult hitResult = Raycast.getHitBlock(player.level(), player, ClipContext.Fluid.NONE, 1000.0);
                    if (hitResult.getType().equals((Object)HitResult.Type.MISS)) {
                        RenderHandler.hasRaycastHitSomething = false;
                    } else {
                        RenderHandler.hasRaycastHitSomething = true;
                        RenderHandler.calculateStartPreviewPositionWithRaycastPosition(hitResult.getBlockPos(), player);
                    }
                }
                if (!hasBuildPlacerSelected) {
                    BuildPlacerHandler.status = "selected";
                    hasBuildPlacerSelected = true;
                    RenderHandler.isUsingPreviewCommand = false;
                }
                player.displayClientMessage((Component)Component.translatable((String)("item.buildpaste.build_placer." + BuildPlacerHandler.status)).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), true);
            } else if (hasBuildPlacerSelected) {
                RenderHandler.stopRendering();
                BuildPlacerHandler.status = "";
                hasBuildPlacerSelected = false;
            }
            lastSelectedItemSlot = currentItemSlot;
        }
        Integer n = tickCount;
        tickCount = tickCount + 1;
    }

    private static enum CLICK_TYPE {
        LEFT,
        RIGHT;

    }
}

