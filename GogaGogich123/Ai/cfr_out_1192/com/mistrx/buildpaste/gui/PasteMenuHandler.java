/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.gui;

import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.util.Functions;
import com.mistrx.buildpaste.util.Variables;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class PasteMenuHandler {
    public static BlockPos pos1;
    public static BlockPos pos2;
    public static String id;
    public static MinecartChest minecart;
    public static ArrayList<String> previousMinecartItemNames;
    public static ArrayList<ItemStack> previousMinecartInventory;
    public static ArrayList<ItemStack> currentMinecartInventory;
    public static boolean clickDetected;
    public static boolean minecartIsBeingOpened;
    public static boolean isOpened;
    public static String currentMenu;
    public static String firstValue;
    public static String secondValue;

    public static void handleClick(final Player player, String itemName, String customName, String color) {
        BuildPasteMod.LOGGER.info("Opening Minecart");
        BuildPasteMod.LOGGER.info(customName);
        BuildPasteMod.LOGGER.info(itemName);
        BuildPasteMod.LOGGER.info(color);
        if (currentMenu.equals("main")) {
            if (customName.equals("Replace Blocks")) {
                if (Variables.blocksInBuild.size() > 0) {
                    minecart.m_6211_();
                    int size = Variables.blocksInBuild.size();
                    if (size > minecart.m_6643_() - 1) {
                        size = minecart.m_6643_() - 1;
                    }
                    int minusOffset = 0;
                    for (int i = 0; i < size; ++i) {
                        String blockName = Variables.blocksInBuild.get(i);
                        try {
                            ResourceLocation resourceLocation = new ResourceLocation("minecraft:" + blockName);
                            Item item = (Item)ForgeRegistries.ITEMS.getValue(resourceLocation);
                            if (item != null && item != Items.f_41852_) {
                                minecart.m_6836_(i - minusOffset, new ItemStack((ItemLike)item));
                                continue;
                            }
                            ItemStack blockNotFoundItemStack = new ItemStack((ItemLike)Items.f_42127_);
                            blockNotFoundItemStack.m_41714_((Component)Component.m_237113_((String)blockName).m_6270_(Style.f_131099_.m_131140_(ChatFormatting.GREEN)));
                            minecart.m_6836_(i - minusOffset, blockNotFoundItemStack);
                            continue;
                        }
                        catch (ResourceLocationException e) {
                            ++minusOffset;
                        }
                    }
                    minecart.m_6836_(minecart.m_6643_() - 1, PasteMenuHandler.GetMenuItem(Items.f_42438_, "Choose Block in Hand", ChatFormatting.AQUA));
                    currentMenu = "replace_blocks_select";
                    PasteMenuHandler.setPreviousMinecart();
                    BuildPasteMod.LOGGER.info(Variables.blocksInBuild);
                }
            } else if (customName.equals("Replace Material - Coming Soon")) {
                player.m_213846_((Component)Component.m_237115_((String)"pastemenu.replace_material.coming_soon"));
            } else if (customName.equals("Undo")) {
                if (Objects.equals(Variables.buildID, id)) {
                    new Timer().schedule(new TimerTask(){

                        @Override
                        public void run() {
                            player.m_213846_((Component)Component.m_237115_((String)"pastemenu.undo.normal"));
                            Functions.pasteCurrentBuilding(Variables.lastPos, Variables.lastPasteDirection, "nopastemodifier", true, false);
                        }
                    }, 1L);
                } else {
                    String command = "/_fillbuild " + Functions.PosToString(new BlockPos(pos1.m_123341_(), pos1.m_123342_(), pos1.m_123343_())) + " " + Functions.PosToString(new BlockPos(pos2.m_123341_(), pos2.m_123342_(), pos2.m_123343_())) + " air";
                    MutableComponent fillAir = Component.m_237113_((String)"Fill with Air");
                    Style fillAirStyle = Style.f_131099_;
                    fillAirStyle = fillAirStyle.m_131157_(ChatFormatting.AQUA);
                    fillAirStyle = fillAirStyle.m_131142_(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
                    fillAirStyle = fillAirStyle.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"Fill the whole area where the build is with air")));
                    fillAir.m_130948_(fillAirStyle);
                    player.m_213846_((Component)Component.m_237110_((String)"pastemenu.undo.fill", (Object[])new Object[]{fillAir}));
                }
            } else if (customName.equals("View in Browser")) {
                Util.m_137581_().m_137646_(Variables.url + "/build?" + id);
                player.m_6915_();
            } else if (customName.equals("3D Print")) {
                String blockprintsUrl = "https://blockprints.net/print/build/" + id;
                player.m_213846_((Component)Component.m_237113_((String)"Create a 3D printed replica of your creation on on blockprints.net"));
                Util.m_137581_().m_137646_(blockprintsUrl);
                player.m_6915_();
            }
        } else if (currentMenu.equals("replace_blocks_select")) {
            if (Objects.equals(itemName, "barrier") && Objects.equals(color, "green")) {
                firstValue = customName;
                BuildPasteMod.LOGGER.info("Barrier as item found, name is: " + firstValue);
            } else if (Objects.equals(itemName, "oak_sign") && customName.equals("Choose Block in Hand")) {
                String name = player.m_21120_(InteractionHand.MAIN_HAND).toString();
                firstValue = name.substring(name.indexOf(" ") + 1);
                BuildPasteMod.LOGGER.info("In Hand: " + firstValue);
            } else {
                firstValue = itemName;
            }
            String[] itemSplit = firstValue.split("_");
            HashMap<String, Integer> replaceWithBlocksMap = new HashMap<String, Integer>();
            for (int i = 0; i < itemSplit.length; ++i) {
                for (int j = 0; j < Functions.blocksArray.length; ++j) {
                    String thisBlock;
                    if (!Functions.blocksArray[j].contains(itemSplit[i]) || replaceWithBlocksMap.containsKey(thisBlock = Functions.getItemFromBlock(Functions.blocksArray[i]))) continue;
                    if (i == 0) {
                        replaceWithBlocksMap.put(Functions.blocksArray[j], 1);
                        continue;
                    }
                    replaceWithBlocksMap.put(Functions.blocksArray[j], 2);
                }
            }
            if (firstValue.contains("water")) {
                replaceWithBlocksMap.put("lava_bucket", 1);
            } else if (firstValue.contains("lava")) {
                replaceWithBlocksMap.put("water_bucket", 1);
            }
            Map<String, Integer> sortedHashMap = PasteMenuHandler.sortByValue(replaceWithBlocksMap, false);
            Object[] mapKeys = sortedHashMap.keySet().toArray();
            BuildPasteMod.LOGGER.info((Object)mapKeys);
            BuildPasteMod.LOGGER.info(replaceWithBlocksMap);
            BuildPasteMod.LOGGER.info(sortedHashMap);
            minecart.m_6211_();
            int size = mapKeys.length;
            if (size > minecart.m_6643_() - 1) {
                size = minecart.m_6643_() - 1;
            }
            int itemsAddedCount = 0;
            for (int i = 0; itemsAddedCount < 27 && i < size && mapKeys[i] instanceof String; ++i) {
                ResourceLocation resourceLocation = new ResourceLocation("minecraft:" + mapKeys[i]);
                Item item = (Item)ForgeRegistries.ITEMS.getValue(resourceLocation);
                if (item == null || item == Items.f_41852_) continue;
                minecart.m_6836_(itemsAddedCount, new ItemStack((ItemLike)item));
                ++itemsAddedCount;
            }
            minecart.m_6836_(minecart.m_6643_() - 1, PasteMenuHandler.GetMenuItem(Items.f_42438_, "Choose Block in Offhand", ChatFormatting.AQUA));
            minecart.m_6836_(minecart.m_6643_() - 2, PasteMenuHandler.GetMenuItem(Items.f_42127_, "Remove Block", ChatFormatting.RED));
            currentMenu = "replace_blocks_with_select";
            PasteMenuHandler.setPreviousMinecart();
        } else if (currentMenu.equals("replace_blocks_with_select")) {
            if (Objects.equals(itemName, "barrier") && customName.equals("Remove Block")) {
                secondValue = "air";
            } else if (Objects.equals(itemName, "oak_sign") && customName.equals("Choose Block in Offhand")) {
                String name = player.m_21120_(InteractionHand.OFF_HAND).toString();
                secondValue = name.substring(name.indexOf(" ") + 1);
                BuildPasteMod.LOGGER.info("In Offhand: " + secondValue);
            } else {
                secondValue = itemName.contains("_bucket") ? itemName.replace("_bucket", "") : itemName;
            }
            String command = "_fillbuild " + Functions.PosToString(new BlockPos(pos1.m_123341_(), pos1.m_123342_(), pos1.m_123343_())) + " " + Functions.PosToString(new BlockPos(pos2.m_123341_(), pos2.m_123342_(), pos2.m_123343_())) + " " + secondValue + " replace " + firstValue;
            try {
                BuildPasteMod.LOGGER.info(command);
                Variables.player.m_20193_().m_7654_().m_129892_().m_82094_().execute(command, (Object)Variables.player.m_20203_());
            }
            catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setPreviousMinecart() {
        if (minecart == null) {
            return;
        }
        previousMinecartInventory.clear();
        previousMinecartItemNames.clear();
        for (int i = 0; i < minecart.m_6643_(); ++i) {
            previousMinecartInventory.add(minecart.m_8020_(i));
            String name = minecart.m_8020_(i).toString();
            previousMinecartItemNames.add(name.substring(name.indexOf(" ") + 1));
        }
        clickDetected = false;
        BuildPasteMod.LOGGER.info(previousMinecartInventory);
    }

    public static ItemStack GetMenuItem(Item item, String name, ChatFormatting color) {
        ItemStack itemStack = new ItemStack((ItemLike)item);
        itemStack.m_41714_((Component)Component.m_237113_((String)name).m_6270_(Style.f_131099_.m_131148_(TextColor.m_131270_((ChatFormatting)color))));
        return itemStack;
    }

    public static void SetMainMenu(Player player) {
        Level world = player.m_9236_();
        if (!world.f_46443_) {
            if (minecart != null) {
                minecart.m_6211_();
                minecart.m_6074_();
            }
            minecart = new MinecartChest(player.m_9236_(), player.m_20185_(), player.m_20186_() + 5.0, player.m_20189_());
            player.m_9236_().m_7967_((Entity)minecart);
            minecart.m_6593_((Component)Component.m_237113_((String)"Buildpaste"));
            minecart.m_20331_(true);
            minecart.m_20242_(true);
            minecart.m_6836_(11, PasteMenuHandler.GetMenuItem(Items.f_42018_, "Replace Blocks", ChatFormatting.LIGHT_PURPLE));
            minecart.m_6836_(12, PasteMenuHandler.GetMenuItem(Items.f_42647_, "Replace Material - Coming Soon", ChatFormatting.LIGHT_PURPLE));
            minecart.m_6836_(13, PasteMenuHandler.GetMenuItem(Items.f_42693_, "Undo", ChatFormatting.LIGHT_PURPLE));
            minecart.m_6836_(14, PasteMenuHandler.GetMenuItem(Items.f_42573_, "View in Browser", ChatFormatting.LIGHT_PURPLE));
            minecart.m_6836_(15, PasteMenuHandler.GetMenuItem(Items.f_42271_, "3D Print", ChatFormatting.GREEN));
            minecartIsBeingOpened = true;
            minecart.m_6096_(player, InteractionHand.MAIN_HAND);
            currentMenu = "main";
        }
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
        clickDetected = false;
        minecartIsBeingOpened = false;
        isOpened = false;
        currentMenu = "";
        firstValue = "";
        secondValue = "";
    }
}

