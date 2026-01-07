/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.gui;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.chat.SimpleComponent;
import com.mistrx.buildpaste.gui.MinecartItem;
import com.mistrx.buildpaste.items.build_placer.BuildPlacerHandler;
import com.mistrx.buildpaste.pasting.AllBlocksArray;
import com.mistrx.buildpaste.player.PlayerData;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mistrx.buildpaste.util.Functions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

public class PasteMenuHandler {
    public static BlockPos pos1;
    public static BlockPos pos2;
    public static String id;
    public static MinecartChest minecart;
    public static ArrayList<String> previousMinecartItemNames;
    public static ArrayList<ItemStack> previousMinecartInventory;
    public static ArrayList<ItemStack> currentMinecartInventory;
    public static ArrayList<MinecartItem> lastMinecartContents;
    public static boolean clickDetected;
    public static boolean minecartIsBeingOpened;
    public static boolean isOpened;
    public static boolean closeMinecartNextTime;
    public static boolean closeContainerNextTime;
    public static String currentLoadedGui;
    public static String currentMenu;
    public static String firstValue;
    public static String secondValue;

    public static void handleClick(Player player, MinecartItem minecartItem) {
        String itemName = minecartItem.getItemName().replaceAll("minecraft:", "");
        String customName = minecartItem.getCustomName();
        String customTag = minecartItem.getCustomTag();
        String color = minecartItem.getColor();
        if (customTag != null) {
            customTag = customTag.substring(customTag.indexOf("-") + 1);
        }
        BuildPasteMod.LOGGER.info("Opening Minecart");
        BuildPasteMod.LOGGER.info("Custom Name: " + customName);
        BuildPasteMod.LOGGER.info("Item Name:" + itemName);
        BuildPasteMod.LOGGER.info("Custom Tag: " + customTag);
        BuildPasteMod.LOGGER.info("Color: " + color);
        PlayerData playerData = PlayerDataManager.getOrCreatePlayerData(player);
        if (currentLoadedGui.equals("paste-menu")) {
            if (currentMenu.equals("main")) {
                if (customTag.equals("replace_blocks")) {
                    if (!playerData.getBlocksInBuild().isEmpty()) {
                        minecart.clearContent();
                        int size = playerData.getBlocksInBuild().size();
                        if (size > minecart.getContainerSize() - 1) {
                            size = minecart.getContainerSize() - 1;
                        }
                        int minusOffset = 0;
                        for (int i = 0; i < size; ++i) {
                            String blockName = playerData.getBlocksInBuild().get(i);
                            try {
                                ResourceLocation resourceLocation = ResourceLocation.parse((String)("minecraft:" + blockName));
                                Item item = (Item)BuiltInRegistries.ITEM.getValue(resourceLocation);
                                if (item != Items.AIR) {
                                    minecart.setItem(i - minusOffset, new ItemStack((ItemLike)item));
                                    continue;
                                }
                                ItemStack blockNotFoundItemStack = new ItemStack((ItemLike)Items.BARRIER);
                                blockNotFoundItemStack.set(DataComponents.CUSTOM_NAME, (Object)Component.literal((String)blockName).withStyle(ChatFormatting.GREEN));
                                minecart.setItem(i - minusOffset, blockNotFoundItemStack);
                                continue;
                            }
                            catch (ResourceLocationException e) {
                                ++minusOffset;
                            }
                        }
                        minecart.setItem(minecart.getContainerSize() - 1, PasteMenuHandler.GetMenuItem(Items.OAK_SIGN, "Choose Block in Hand", "replace_1-choose_main_hand", ChatFormatting.AQUA));
                        currentMenu = "replace_blocks_select";
                        PasteMenuHandler.CollectLastMinecartContents();
                    }
                    return;
                }
                if (customTag.equals("replace_materials")) {
                    player.displayClientMessage((Component)Component.translatable((String)"pastemenu.replace_material.coming_soon"), false);
                } else if (customTag.equals("undo")) {
                    if (Objects.equals(playerData.getSelectedBuildId(), id)) {
                        assert (Minecraft.getInstance().player != null);
                        Minecraft.getInstance().player.connection.sendCommand("undopaste");
                        player.displayClientMessage((Component)Component.translatable((String)"commands.undo.success"), false);
                    } else {
                        String command = "/_fillbuild " + Functions.PosToString(new BlockPos(pos1.getX(), pos1.getY(), pos1.getZ())) + " " + Functions.PosToString(new BlockPos(pos2.getX(), pos2.getY(), pos2.getZ())) + " air";
                        SimpleComponent.TextBuilder fillAir = SimpleComponent.text("Fill with air", ChatFormatting.AQUA).clickRunCommand(command).hoverText("Fill the whole area where the build is with air");
                        player.displayClientMessage((Component)Component.translatable((String)"pastemenu.undo.fill", (Object[])new Object[]{fillAir}), false);
                    }
                } else if (customTag.equals("view_browser")) {
                    Util.getPlatform().openUri("https://buildpaste.net/build?" + id);
                    player.closeContainer();
                } else if (customTag.equals("select_for_upload")) {
                    PlayerDataManager.getOrCreatePlayerData(player).getUploadingDataStore().setPos1(new Vector3d((double)pos1.getX(), (double)pos1.getY(), (double)pos1.getZ()));
                    PlayerDataManager.getOrCreatePlayerData(player).getUploadingDataStore().setPos2(new Vector3d((double)pos2.getX(), (double)pos2.getY(), (double)pos2.getZ()));
                    player.displayClientMessage((Component)Component.translatable((String)"pastemenu.select_for_update.notice"), false);
                } else if (customName.equals("3D Print")) {
                    String blockprintsUrl = "https://blockprints.net/print/build/" + id;
                    player.displayClientMessage((Component)Component.literal((String)"Create a 3D printed replica of your creation on on blockprints.net"), false);
                    Util.getPlatform().openUri(blockprintsUrl);
                    player.closeContainer();
                }
                PasteMenuHandler.CloseMenu(player);
            } else if (currentMenu.equals("replace_blocks_select")) {
                if (Objects.equals(itemName, "barrier") && Objects.equals(color, "green")) {
                    firstValue = customName;
                    BuildPasteMod.LOGGER.info("Barrier as item found, name is: " + firstValue);
                } else if (Objects.equals(itemName, "oak_sign") && customName.equals("Choose Block in Hand")) {
                    String name = player.getItemInHand(InteractionHand.MAIN_HAND).toString();
                    firstValue = name.substring(name.indexOf(" ") + 1);
                    BuildPasteMod.LOGGER.info("In Hand: " + firstValue);
                } else {
                    firstValue = itemName;
                }
                firstValue = firstValue.replace("minecraft:", "");
                String[] itemSplit = firstValue.split("_");
                HashMap<String, Integer> replaceWithBlocksMap = new HashMap<String, Integer>();
                for (int i = 0; i < itemSplit.length; ++i) {
                    for (int j = 0; j < AllBlocksArray.blocksArray.length; ++j) {
                        String thisBlock;
                        if (!AllBlocksArray.blocksArray[j].contains(itemSplit[i]) || replaceWithBlocksMap.containsKey(thisBlock = Functions.getItemFromBlock(AllBlocksArray.blocksArray[i]))) continue;
                        if (i == 0) {
                            replaceWithBlocksMap.put(AllBlocksArray.blocksArray[j], 1);
                            continue;
                        }
                        replaceWithBlocksMap.put(AllBlocksArray.blocksArray[j], 2);
                    }
                }
                if (firstValue.contains("water")) {
                    replaceWithBlocksMap.put("lava_bucket", 1);
                } else if (firstValue.contains("lava")) {
                    replaceWithBlocksMap.put("water_bucket", 1);
                }
                Map<String, Integer> sortedHashMap = PasteMenuHandler.sortByValue(replaceWithBlocksMap, false);
                Object[] mapKeys = sortedHashMap.keySet().toArray();
                minecart.clearContent();
                int size = mapKeys.length;
                if (size > minecart.getContainerSize() - 1) {
                    size = minecart.getContainerSize() - 1;
                }
                int itemsAddedCount = 0;
                for (int i = 0; itemsAddedCount < 27 && i < size && mapKeys[i] instanceof String; ++i) {
                    ResourceLocation resourceLocation = ResourceLocation.parse((String)("minecraft:" + String.valueOf(mapKeys[i])));
                    Item item = (Item)BuiltInRegistries.ITEM.getValue(resourceLocation);
                    if (item == Items.AIR) continue;
                    BuildPasteMod.LOGGER.info("ADDING NOW");
                    minecart.setItem(itemsAddedCount, new ItemStack((ItemLike)item));
                    ++itemsAddedCount;
                }
                minecart.setItem(minecart.getContainerSize() - 1, PasteMenuHandler.GetMenuItem(Items.OAK_SIGN, "Choose Block in Offhand", "replace_2-choose_offhand", ChatFormatting.AQUA));
                minecart.setItem(minecart.getContainerSize() - 2, PasteMenuHandler.GetMenuItem(Items.BARRIER, "Remove Block", "replace_2-remove_block", ChatFormatting.RED));
                currentMenu = "replace_blocks_with_select";
                PasteMenuHandler.CollectLastMinecartContents();
            } else if (currentMenu.equals("replace_blocks_with_select")) {
                if (Objects.equals(itemName, "barrier") && customName.equals("Remove Block")) {
                    secondValue = "air";
                } else if (Objects.equals(itemName, "oak_sign") && customName.equals("Choose Block in Offhand")) {
                    String name = player.getItemInHand(InteractionHand.OFF_HAND).toString();
                    secondValue = name.substring(name.indexOf(" ") + 1);
                    BuildPasteMod.LOGGER.info("In Offhand: " + secondValue);
                } else {
                    secondValue = itemName.contains("_bucket") ? itemName.replace("_bucket", "") : itemName;
                }
                String command = "_fillbuild " + Functions.PosToString(new BlockPos(pos1.getX(), pos1.getY(), pos1.getZ())) + " " + Functions.PosToString(new BlockPos(pos2.getX(), pos2.getY(), pos2.getZ())) + " " + secondValue + " replace " + firstValue;
                assert (Minecraft.getInstance().player != null);
                Minecraft.getInstance().player.connection.sendCommand(command);
                player.displayClientMessage((Component)Component.literal((String)("Replaced " + firstValue.replace("minecraft:", "") + " with " + secondValue.replace("minecraft:", ""))), true);
                PasteMenuHandler.CloseMenu(player);
            }
        } else if (currentLoadedGui.equals("build-placer-menu") && currentMenu.equals("main")) {
            String translatableComponentToSend = null;
            if (customName.startsWith("Bind Current Build")) {
                assert (Minecraft.getInstance().player != null);
                Minecraft.getInstance().player.connection.sendCommand("bindbuild");
            } else if (customName.startsWith("Randomly Rotate Build")) {
                BuildPasteMod.LOGGER.info("Current do random rotate: " + BuildPlacerHandler.getTag(player, "buildpaste.do-random-rotate"));
                if (Objects.equals(BuildPlacerHandler.getTag(player, "buildpaste.do-random-rotate"), "true")) {
                    BuildPlacerHandler.setTag(player, "buildpaste.do-random-rotate", "false");
                    BuildPlacerHandler.currentDoRandomRotation = false;
                    translatableComponentToSend = "gui.buildplacer.rotate.false";
                } else {
                    BuildPlacerHandler.setTag(player, "buildpaste.do-random-rotate", "true");
                    BuildPlacerHandler.currentDoRandomRotation = true;
                    translatableComponentToSend = "gui.buildplacer.rotate.true";
                }
            } else if (customName.startsWith("Place Build one below")) {
                if (Objects.equals(BuildPlacerHandler.getTag(player, "buildpaste.do-place-one-block-below"), "true")) {
                    BuildPlacerHandler.setTag(player, "buildpaste.do-place-one-block-below", "false");
                    BuildPlacerHandler.currentDoPlaceOneBlockBelow = false;
                    translatableComponentToSend = "gui.buildplacer.below.false";
                } else {
                    BuildPlacerHandler.setTag(player, "buildpaste.do-place-one-block-below", "true");
                    BuildPlacerHandler.currentDoPlaceOneBlockBelow = true;
                    translatableComponentToSend = "gui.buildplacer.below.true";
                }
            } else if (customName.startsWith("Place Air")) {
                if (Objects.equals(BuildPlacerHandler.getTag(player, "buildpaste.do-place-air"), "true")) {
                    BuildPlacerHandler.setTag(player, "buildpaste.do-place-air", "false");
                    BuildPlacerHandler.currentDoPlaceAir = false;
                    translatableComponentToSend = "gui.buildplacer.placeair.false";
                } else {
                    BuildPlacerHandler.setTag(player, "buildpaste.do-place-air", "true");
                    BuildPlacerHandler.currentDoPlaceAir = true;
                    translatableComponentToSend = "gui.buildplacer.placeair.true";
                }
            } else if (customName.startsWith("Enable 'Quick Actions' after pasting")) {
                if (Objects.equals(BuildPlacerHandler.getTag(player, "buildpaste.do-send-success-message"), "true")) {
                    BuildPlacerHandler.setTag(player, "buildpaste.do-send-success-message", "false");
                    BuildPlacerHandler.currentDoSendSuccessMessage = false;
                    translatableComponentToSend = "gui.buildplacer.sendsuccess.false";
                } else {
                    BuildPlacerHandler.setTag(player, "buildpaste.do-send-success-message", "true");
                    BuildPlacerHandler.currentDoSendSuccessMessage = true;
                    translatableComponentToSend = "gui.buildplacer.sendsuccess.true";
                }
            }
            if (translatableComponentToSend != null) {
                player.displayClientMessage((Component)Component.translatable(translatableComponentToSend), false);
            }
            PasteMenuHandler.CloseMenu(player);
        }
    }

    public static void CollectLastMinecartContents() {
        BuildPasteMod.LOGGER.info("CollectLastMinecartContents Beginning");
        if (minecart == null) {
            return;
        }
        previousMinecartInventory.clear();
        previousMinecartItemNames.clear();
        lastMinecartContents.clear();
        for (int i = 0; i < minecart.getContainerSize(); ++i) {
            ItemStack minecartItem = minecart.getItem(i);
            previousMinecartInventory.add(minecartItem);
            String name = minecart.getItem(i).toString();
            previousMinecartItemNames.add(name.substring(name.indexOf(" ") + 1));
            String customTag = null;
            if (minecartItem.get(DataComponents.CUSTOM_DATA) != null) {
                customTag = (String)((CustomData)minecartItem.get(DataComponents.CUSTOM_DATA)).copyTag().getString("menu.action").get();
            }
            String color = "";
            if (minecartItem.getHoverName().getStyle().getColor() != null) {
                color = minecartItem.getHoverName().getStyle().getColor().toString();
            }
            lastMinecartContents.add(new MinecartItem(name.substring(name.indexOf(" ") + 1), minecartItem.getHoverName().getString(), minecartItem.getCount(), color, customTag));
        }
        clickDetected = false;
        BuildPasteMod.LOGGER.info("CollectLastMinecartContents: " + previousMinecartInventory.toString());
    }

    public static ItemStack GetMenuItem(Item item, String name, String customTag, ChatFormatting color) {
        ItemStack itemStack = new ItemStack((ItemLike)item);
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("menu.action", customTag);
        BuildPasteMod.LOGGER.info("Putting: " + customTag);
        CustomData.set((DataComponentType)DataComponents.CUSTOM_DATA, (ItemStack)itemStack, (CompoundTag)compoundTag);
        itemStack.set(DataComponents.CUSTOM_NAME, (Object)Component.literal((String)name).withStyle(color));
        return itemStack;
    }

    public static ItemStack GetMenuItem(Item item, String name, String customTag, ChatFormatting color, boolean isActive) {
        ItemStack itemStack = new ItemStack((ItemLike)item);
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("menu.action", customTag);
        if (isActive) {
            compoundTag.putString("menu.active", "true");
        } else {
            compoundTag.putString("menu.active", "false");
        }
        CustomData.set((DataComponentType)DataComponents.CUSTOM_DATA, (ItemStack)itemStack, (CompoundTag)compoundTag);
        itemStack.set(DataComponents.CUSTOM_NAME, (Object)Component.literal((String)name).withStyle(color));
        return itemStack;
    }

    public static void SetMainMenu(String gui, Player player) {
        Level world = player.level();
        if (minecart != null) {
            minecart.clearContent();
            if (world instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)world;
                minecart.kill(serverLevel);
            }
        }
        minecart = new MinecartChest(EntityType.CHEST_MINECART, world);
        world.addFreshEntity((Entity)minecart);
        minecart.setPos(player.getX(), player.getY() + 5.0, player.getZ());
        minecart.setCustomName((Component)Component.literal((String)"Buildpaste"));
        minecart.setInvulnerable(true);
        minecart.setNoGravity(true);
        minecart.setInvisible(true);
        if (Objects.equals(gui, "paste-menu")) {
            minecart.setItem(12, PasteMenuHandler.GetMenuItem(Items.STONE_BRICKS, "Replace Blocks", "paste_menu-replace_blocks", ChatFormatting.LIGHT_PURPLE));
            minecart.setItem(13, PasteMenuHandler.GetMenuItem(Items.OAK_PLANKS, "Replace Materials - Coming Soon", "paste_menu-replace_materials", ChatFormatting.LIGHT_PURPLE));
            minecart.setItem(14, PasteMenuHandler.GetMenuItem(Items.TNT_MINECART, "Undo", "paste_menu-undo", ChatFormatting.RED));
        } else if (gui.equals("build-placer-menu")) {
            BuildPlacerHandler.loadBuildPlacerVariables(player);
            String nameExtensionWhenFreeMember = "";
            minecart.setItem(11, PasteMenuHandler.GetMenuItem(Items.END_CRYSTAL, "Bind Current Build" + nameExtensionWhenFreeMember, "build_placer-bind_build", ChatFormatting.GOLD));
            minecart.setItem(12, PasteMenuHandler.GetMenuItem(Items.MAGENTA_GLAZED_TERRACOTTA, "Randomly Rotate Build" + nameExtensionWhenFreeMember, "build_placer-random_rotate", ChatFormatting.GOLD, BuildPlacerHandler.currentDoRandomRotation));
            minecart.setItem(13, PasteMenuHandler.GetMenuItem(Items.GRASS_BLOCK, "Place Build one below" + nameExtensionWhenFreeMember, "build_placer-place_one_below", ChatFormatting.GOLD, BuildPlacerHandler.currentDoPlaceOneBlockBelow));
            minecart.setItem(14, PasteMenuHandler.GetMenuItem(Items.WHITE_WOOL, "Place Air" + nameExtensionWhenFreeMember, "build_placer-place_air", ChatFormatting.GOLD, BuildPlacerHandler.currentDoPlaceAir));
            minecart.setItem(15, PasteMenuHandler.GetMenuItem(Items.OAK_SIGN, "Enable 'Quick Actions' after pasting" + nameExtensionWhenFreeMember, "build_placer-quick_actions", ChatFormatting.GOLD, BuildPlacerHandler.currentDoSendSuccessMessage));
        }
        currentLoadedGui = gui;
        minecartIsBeingOpened = true;
        minecart.interact(player, InteractionHand.MAIN_HAND);
        PasteMenuHandler.CollectLastMinecartContents();
        currentMenu = "main";
    }

    private static void CloseMenu(Player player) {
        closeContainerNextTime = true;
    }

    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap, boolean order) {
        LinkedList<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());
        list.sort((o1, o2) -> order ? (((Integer)o1.getValue()).compareTo((Integer)o2.getValue()) == 0 ? ((String)o1.getKey()).compareTo((String)o2.getKey()) : ((Integer)o1.getValue()).compareTo((Integer)o2.getValue())) : (((Integer)o2.getValue()).compareTo((Integer)o1.getValue()) == 0 ? ((String)o2.getKey()).compareTo((String)o1.getKey()) : ((Integer)o2.getValue()).compareTo((Integer)o1.getValue())));
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }

    static {
        minecart = null;
        previousMinecartItemNames = new ArrayList();
        previousMinecartInventory = new ArrayList();
        currentMinecartInventory = new ArrayList();
        lastMinecartContents = new ArrayList();
        clickDetected = false;
        minecartIsBeingOpened = false;
        isOpened = false;
        closeMinecartNextTime = false;
        closeContainerNextTime = false;
        currentLoadedGui = "";
        currentMenu = "";
        firstValue = "";
        secondValue = "";
    }
}

