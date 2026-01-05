/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.util;

import com.google.gson.JsonObject;
import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.firebase.BuildDataStore;
import com.mistrx.buildpaste.firebase.UndoBuildDataStore;
import com.mistrx.buildpaste.pasting.AllBlocksArray;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mistrx.buildpaste.util.CommandChainLengthHandler;
import com.mistrx.buildpaste.util.Variables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public class Functions {
    public static Integer[] pasteBlockBlackListArray = new Integer[0];
    public static ArrayList<Integer> pasteBlockBlackList = new ArrayList<Integer>(Arrays.asList(pasteBlockBlackListArray));
    public static String[] blockDataBlacklist = new String[]{"level=0", "snowy=false", "waterlogged=true"};
    public static int newerBlocksAmountFound = 0;
    public static ArrayList<BlockPos> observerBlockPos = new ArrayList();
    public static ArrayList<BlockState> observerBlockStates = new ArrayList();
    public static HashMap<String, String> blockEqualsItemHashmap = new HashMap();
    public static boolean hasFoundOldNBT = false;
    public static List<String> buildsWithOldNBT = new ArrayList<String>();

    public static void setItemEqualsBlockHashmap() {
        Object toAdd = "";
        toAdd = (String)toAdd + "attached_melon_stem=melon_seeds\n";
        toAdd = (String)toAdd + "attached_pumpkin_stem=pumpkin_seeds\n";
        toAdd = (String)toAdd + "melon_stem=melon_seeds\n";
        toAdd = (String)toAdd + "pumpkin_stem=pumpkin_seeds\n";
        toAdd = (String)toAdd + "bamboo_sapling=bamboo\n";
        toAdd = (String)toAdd + "chorus_plant=chorus_fruit\n";
        toAdd = (String)toAdd + "cocoa=cocoa_beans\n";
        toAdd = (String)toAdd + "farmland=dirt\n";
        toAdd = (String)toAdd + "frosted_ice=ice\n";
        toAdd = (String)toAdd + "grass_path=grass\n";
        toAdd = (String)toAdd + "kelp_plant=kelp\n";
        toAdd = (String)toAdd + "moving_piston=piston\n";
        toAdd = (String)toAdd + "redstone_wire=redstone\n";
        toAdd = (String)toAdd + "snow=snowball\n";
        toAdd = (String)toAdd + "sweet_berry_bush=sweet_berries\n";
        toAdd = (String)toAdd + "beetroots=beetroot\n";
        toAdd = (String)toAdd + "potatoes=potato\n";
        toAdd = (String)toAdd + "carrots=carrot\n";
        toAdd = (String)toAdd + "wheat=wheat_seeds\n";
        String[] toAddArray = ((String)toAdd).split("\n");
        for (int i = 0; i < toAddArray.length; ++i) {
            String[] keyAndValue = toAddArray[i].split("=");
            blockEqualsItemHashmap.put("minecraft:" + keyAndValue[0], "minecraft:" + keyAndValue[1]);
        }
    }

    public static String getItemFromBlock(String block) {
        if (blockEqualsItemHashmap.containsKey(block)) {
            return blockEqualsItemHashmap.get(block);
        }
        if (block.contains("wall_")) {
            return block.replace("wall_", "");
        }
        if (block.contains("infested_")) {
            return block.replace("infested_", "");
        }
        if (block.contains("potted_")) {
            return "flower_pot";
        }
        return block;
    }

    public static String getBlockData(Level world, Vector3d pos) {
        BlockState state = world.getBlockState(new BlockPos((int)pos.x, (int)pos.y, (int)pos.z));
        int length = state.getValues().keySet().toArray().length;
        Object data = "";
        for (int i = 0; i < length; ++i) {
            String thisKey = state.getValues().keySet().toArray()[i].toString();
            thisKey = thisKey.substring(thisKey.indexOf("=") + 1, thisKey.indexOf(",")).toLowerCase();
            String thisValue = state.getValues().values().toArray()[i].toString().toLowerCase();
            String thisData = thisKey + "=" + thisValue;
            boolean isBlacklisted = false;
            for (int j = 0; j < blockDataBlacklist.length; ++j) {
                if (!thisData.contains(blockDataBlacklist[j])) continue;
                isBlacklisted = true;
            }
            if (i == 0) {
                data = (String)data + "[";
            }
            if (i == length - 1) {
                if (isBlacklisted) {
                    if (((String)data).endsWith(",")) {
                        data = ((String)data).substring(0, ((String)data).length() - 1);
                    }
                } else {
                    data = (String)data + thisData;
                }
                data = (String)data + "]";
                continue;
            }
            data = (String)data + thisData + ",";
        }
        if (data == "[]" || ((String)data).equals("") || ((String)data).length() == 2) {
            return null;
        }
        return data;
    }

    public static String getSetblockCommand(Vector3d pos, String blockname, String data, String nbt) {
        int x = Math.toIntExact(Math.round(pos.x));
        int y = Math.toIntExact(Math.round(pos.y));
        int z = Math.toIntExact(Math.round(pos.z));
        if (data == null || data.equals("null") || !data.startsWith("[")) {
            data = "";
        }
        Object command = "";
        command = "_setblocknoresponse " + x + " " + y + " " + z + " " + blockname + data + nbt;
        return command;
    }

    public static void setBlock(Level world, Vector3d currentPos, String placeBlockName, String data, String nbt, String direction, String uploadDirection, String pasteModifier) {
        if (Functions.includesOldNBT(placeBlockName, nbt) && !hasFoundOldNBT) {
            hasFoundOldNBT = true;
        }
        if (pasteModifier.equals("dontplaceair") && placeBlockName.equals("minecraft:air")) {
            return;
        }
        if (placeBlockName.equals("minecraft:observer")) {
            BlockPos blockPos = new BlockPos((int)currentPos.x, (int)currentPos.y, (int)currentPos.z);
            String rotation = Functions.rotateBlock(data, direction, uploadDirection, true);
            Direction dir = Functions.getDirection(rotation);
            BlockState observerState = (BlockState)Blocks.OBSERVER.defaultBlockState().setValue((Property)BlockStateProperties.FACING, (Comparable)dir);
            observerBlockPos.add(blockPos);
            observerBlockStates.add(observerState);
            world.setBlockAndUpdate(blockPos, Blocks.STONE.defaultBlockState());
            return;
        }
        String thisData = Functions.rotateBlock(data, direction, uploadDirection);
        String command = Functions.getSetblockCommand(currentPos, placeBlockName, thisData, nbt);
        Functions.executeCommand(world, command);
    }

    @NotNull
    private static Direction getDirection(String rotation) {
        return switch (rotation) {
            case "east" -> Direction.EAST;
            case "west" -> Direction.WEST;
            case "north" -> Direction.NORTH;
            case "up" -> Direction.UP;
            case "down" -> Direction.DOWN;
            default -> Direction.SOUTH;
        };
    }

    public static void pasteBuild(final Player player, BuildDataStore buildData, Vector3d pasteBeginPos, String direction, String pasteModifier, Boolean isConstructing) throws Exception {
        Vector3d lastBlockPlacePos;
        Vector3d firstBlockPlacePos;
        int b2;
        int a2;
        int c1;
        int b1;
        int a1;
        if (player == null) {
            throw new Exception("pastebuild/error-player-null");
        }
        if (buildData == null) {
            throw new Exception("pastebuild/error-builddata-null");
        }
        if (pasteBeginPos == null) {
            throw new Exception("pastebuild/error-pos-null");
        }
        if (direction == null) {
            throw new Exception("pastebuild/error-direction-null");
        }
        Vector3d size = buildData.getSize();
        List<Object> ids = buildData.getBlockIds();
        List<String> data = buildData.getBlockData();
        JsonObject nbt = buildData.getBlockNBT();
        String uploadDirection = buildData.getUploadDirection();
        newerBlocksAmountFound = 0;
        hasFoundOldNBT = false;
        Vector3d lastBuildSize = buildData.getSize();
        ArrayList<Object> lastBlockIDs = new ArrayList<Object>();
        ArrayList<String> lastBlockData = new ArrayList<String>();
        HashMap<String, Integer> usedMaterials = new HashMap<String, Integer>();
        ArrayList<String> blocksInBuild = new ArrayList<String>();
        int sx = (int)size.x;
        int sy = (int)size.y;
        int sz = (int)size.z;
        int ox = (int)pasteBeginPos.x;
        int oy = (int)pasteBeginPos.y;
        int oz = (int)pasteBeginPos.z;
        boolean special = buildData.getUploadDirection().equals("north") || buildData.getUploadDirection().equals("south");
        int c2 = switch (direction) {
            case "north" -> {
                if (special) {
                    a1 = -1;
                    b1 = 0;
                    c1 = sx;
                    a2 = 0;
                    b2 = -1;
                    yield 0;
                }
                a1 = 0;
                b1 = -1;
                c1 = sz;
                a2 = -1;
                b2 = 0;
                yield 0;
            }
            case "south" -> {
                if (special) {
                    a1 = 1;
                    b1 = 0;
                    c1 = -sx;
                    a2 = 0;
                    b2 = 1;
                    yield 0;
                }
                a1 = 0;
                b1 = 1;
                c1 = -sz;
                a2 = 1;
                b2 = 0;
                yield 0;
            }
            case "east" -> {
                if (special) {
                    a1 = 0;
                    b1 = 1;
                    c1 = 0;
                    a2 = -1;
                    b2 = 0;
                    yield sx;
                }
                a1 = 1;
                b1 = 0;
                c1 = 0;
                a2 = 0;
                b2 = -1;
                yield sz;
            }
            case "west" -> {
                if (special) {
                    a1 = 0;
                    b1 = -1;
                    c1 = 0;
                    a2 = 1;
                    b2 = 0;
                    yield -sx;
                }
                a1 = -1;
                b1 = 0;
                c1 = 0;
                a2 = 0;
                b2 = 1;
                yield -sz;
            }
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        };
        int i = 0;
        for (int x = 0; x < sx; ++x) {
            for (int y = 0; y < sy; ++y) {
                for (int z = 0; z < sz; ++z) {
                    String previousBlockName;
                    String blockWithoutOriginPrefix;
                    int dx = a1 * x + b1 * z + c1;
                    int dz = a2 * x + b2 * z + c2;
                    Vector3d blockPos = new Vector3d((double)(ox + dx), (double)(oy + y), (double)(oz + dz));
                    String block = Functions.getBlockByIdOrName(ids.get(i).toString());
                    String nbtData = "";
                    if (nbt.has(String.valueOf(i))) {
                        nbtData = nbt.get(String.valueOf(i)).getAsString();
                    }
                    if (!blocksInBuild.contains(blockWithoutOriginPrefix = block.replace("minecraft:", "")) && !blockWithoutOriginPrefix.equals("air")) {
                        blocksInBuild.add(blockWithoutOriginPrefix);
                    }
                    if (Functions.getBlockIdByName(previousBlockName = Functions.getBlockNameAtPosition(player.level(), blockPos)) == null) {
                        lastBlockIDs.add(previousBlockName);
                    } else {
                        lastBlockIDs.add(Functions.getBlockIdByName(previousBlockName));
                    }
                    lastBlockData.add(Functions.getBlockData(player.level(), blockPos));
                    if (!previousBlockName.equals(block)) {
                        if (isConstructing.booleanValue()) {
                            String itemFromBlock = Functions.getItemFromBlock(block);
                            if (Variables.playerItems.containsKey(itemFromBlock) && Variables.playerItems.get(itemFromBlock) > 0) {
                                Functions.setBlock(player.level(), blockPos, block, data.get(i), nbtData, direction, uploadDirection, pasteModifier);
                                Variables.playerItems.merge(itemFromBlock, -1, Integer::sum);
                                usedMaterials.merge(itemFromBlock, 1, Integer::sum);
                            }
                        } else {
                            Functions.setBlock(player.level(), blockPos, block, data.get(i), nbtData, direction, uploadDirection, pasteModifier);
                        }
                    }
                    ++i;
                }
            }
        }
        if (!observerBlockPos.isEmpty()) {
            final Level world = player.level();
            new Timer().schedule(new TimerTask(){

                @Override
                public void run() {
                    for (int j = 0; j < observerBlockPos.size(); ++j) {
                        world.setBlockAndUpdate(observerBlockPos.get(j), (BlockState)observerBlockStates.get(j).setValue((Property)BlockStateProperties.POWERED, (Comparable)Boolean.valueOf(false)));
                    }
                    observerBlockPos = new ArrayList();
                    observerBlockStates = new ArrayList();
                }
            }, 1L);
        }
        if (isConstructing.booleanValue()) {
            for (String item : usedMaterials.keySet()) {
                int amount = (Integer)usedMaterials.get(item);
                String command = "_clearnoresponse " + PlayerDataManager.getOrCreatePlayerData(player).getMcname() + " " + item + " " + String.valueOf(amount);
                Functions.executeCommand(player.level(), command);
            }
        }
        Vector3d adjustedSize = size;
        if (buildData.getUploadDirection().equals("east") || buildData.getUploadDirection().equals("west")) {
            adjustedSize = new Vector3d(size.z, size.y, size.x);
        }
        if (direction.equals("north")) {
            firstBlockPlacePos = new Vector3d(pasteBeginPos.x + 1.0, pasteBeginPos.y, pasteBeginPos.z);
            lastBlockPlacePos = Functions.getLastSecondPos(firstBlockPlacePos, adjustedSize, new Vector3d(1.0, 1.0, -1.0));
        } else if (direction.equals("east")) {
            Vector3d modifiedSize = new Vector3d(adjustedSize.z, adjustedSize.y, adjustedSize.x);
            firstBlockPlacePos = new Vector3d(pasteBeginPos.x, pasteBeginPos.y, pasteBeginPos.z + 1.0);
            lastBlockPlacePos = Functions.getLastSecondPos(firstBlockPlacePos, modifiedSize, new Vector3d(1.0, 1.0, 1.0));
        } else if (direction.equals("west")) {
            Vector3d modifiedSize = new Vector3d(adjustedSize.z, adjustedSize.y, adjustedSize.x);
            firstBlockPlacePos = new Vector3d(pasteBeginPos.x, pasteBeginPos.y, pasteBeginPos.z - 1.0);
            lastBlockPlacePos = Functions.getLastSecondPos(firstBlockPlacePos, modifiedSize, new Vector3d(-1.0, 1.0, -1.0));
        } else {
            firstBlockPlacePos = new Vector3d(pasteBeginPos.x - 1.0, pasteBeginPos.y, pasteBeginPos.z);
            lastBlockPlacePos = Functions.getLastSecondPos(firstBlockPlacePos, adjustedSize, new Vector3d(-1.0, 1.0, 1.0));
        }
        PlayerDataManager.getOrCreatePlayerData(player).setLastFirstPos(firstBlockPlacePos);
        PlayerDataManager.getOrCreatePlayerData(player).setLastSecondPos(lastBlockPlacePos);
        UndoBuildDataStore undoBuildDataStore = new UndoBuildDataStore(new BuildDataStore(lastBlockIDs, lastBlockData, nbt, lastBuildSize, uploadDirection), direction, pasteBeginPos);
        PlayerDataManager.getOrCreatePlayerData(player).setUndoBuildData(undoBuildDataStore);
        PlayerDataManager.getOrCreatePlayerData(player).setBlocksInBuild(blocksInBuild);
        new Timer().schedule(new TimerTask(){

            @Override
            public void run() {
                if (hasFoundOldNBT && !buildsWithOldNBT.contains(PlayerDataManager.getOrCreatePlayerData(player).getSelectedBuildId())) {
                    assert (Minecraft.getInstance().player != null);
                    MutableComponent oldNbtWarningComponent = Component.translatable((String)"paste.old_nbt");
                    Style style = Style.EMPTY;
                    style = style.withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.translatable((String)"paste.old_nbt.hover").withStyle(ChatFormatting.WHITE)));
                    oldNbtWarningComponent.setStyle(style);
                    player.displayClientMessage((Component)oldNbtWarningComponent, false);
                }
                buildsWithOldNBT.add(PlayerDataManager.getOrCreatePlayerData(player).getSelectedBuildId());
            }
        }, 20L);
        CommandChainLengthHandler.verifyMaxCommandChainLength(player);
    }

    private static boolean includesOldNBT(String blockId, String nbt) {
        String blockName = blockId.replace("minecraft:", "");
        if (nbt.contains("Count:") && (nbt.contains("AttributeModifiers:") || nbt.contains("Potion") || nbt.contains("Enchantments:"))) {
            return true;
        }
        if (blockName.contains("command_block")) {
            try {
                int endIndex;
                String commandKey = "Command:";
                int startIndex = nbt.indexOf(commandKey);
                if (startIndex == -1) {
                    throw new IllegalArgumentException("The 'Command:' key was not found.");
                }
                char quoteCharacter = nbt.charAt(startIndex += commandKey.length());
                if (quoteCharacter != '\'' && quoteCharacter != '\"') {
                    throw new IllegalArgumentException("Unexpected character after 'Command:'. Expected a quote.");
                }
                if ((endIndex = nbt.indexOf(quoteCharacter, ++startIndex)) == -1) {
                    throw new IllegalArgumentException("Closing quote for the command value was not found.");
                }
                String commandValue = nbt.substring(startIndex, endIndex);
                if (commandValue.contains("{") && commandValue.contains("}")) {
                    return true;
                }
            }
            catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    private static Vector3d getLastSecondPos(Vector3d firstPos, Vector3d size, Vector3d positiveOrNegative) {
        return new Vector3d(firstPos.x + positiveOrNegative.x * (size.x - 1.0), firstPos.y + positiveOrNegative.y * (size.y - 1.0), firstPos.z + positiveOrNegative.z * (size.z - 1.0));
    }

    public static String rotateBlock(String thisData, String pasteDirection, String uploadDirection, boolean returnDirection) {
        if (thisData != null) {
            if (thisData.contains("facing=") && thisData.length() >= 11) {
                String blockDirection = thisData.substring(thisData.indexOf("facing=") + 7);
                blockDirection = blockDirection.contains(",") ? blockDirection.substring(0, blockDirection.indexOf(",")) : blockDirection.substring(0, blockDirection.indexOf("]"));
                return Functions.calculateBlockRotation(blockDirection, pasteDirection, uploadDirection);
            }
        } else {
            return "";
        }
        return thisData;
    }

    public static String rotateBlock(String thisData, String pasteDirection, String uploadDirection) {
        if (thisData != null) {
            if (thisData.contains("facing=") && thisData.length() >= 11) {
                String blockDirection = thisData.substring(thisData.indexOf("facing=") + 7);
                blockDirection = blockDirection.contains(",") ? blockDirection.substring(0, blockDirection.indexOf(",")) : blockDirection.substring(0, blockDirection.indexOf("]"));
                String finalBlockDirection = Functions.calculateBlockRotation(blockDirection, pasteDirection, uploadDirection);
                thisData = thisData.replace("facing=" + blockDirection, "facing=" + finalBlockDirection);
                return thisData;
            }
        } else {
            return "";
        }
        return thisData;
    }

    public static String calculateBlockRotation(String blockDir, String pasteDir, String uploadDir) {
        String[] possibleBlockDirectionsArray = new String[]{"north", "south", "east", "west"};
        List<String> possibleBlockDirections = Arrays.asList(possibleBlockDirectionsArray);
        if (possibleBlockDirections.contains(blockDir)) {
            Integer uploadDirectionInDegrees = Functions.facingToDegrees(uploadDir);
            Integer pasteDirInt = Functions.facingToDegrees(pasteDir);
            Integer differenceUploadPasteDirection = pasteDirInt - uploadDirectionInDegrees;
            Integer result = (360 + (Functions.facingToDegrees(blockDir) + differenceUploadPasteDirection)) % 360;
            return Functions.degreesToFacing(result);
        }
        return blockDir;
    }

    public static Integer facingToDegrees(String facing) {
        switch (facing) {
            case "north": {
                return 0;
            }
            case "east": {
                return 90;
            }
            case "south": {
                return 180;
            }
            case "west": {
                return 270;
            }
        }
        BuildPasteMod.LOGGER.info("No rotation is passend, return 0");
        return 0;
    }

    public static String degreesToFacing(Integer degrees) {
        if (degrees == 0) {
            return "north";
        }
        if (degrees == 90) {
            return "east";
        }
        if (degrees == 180) {
            return "south";
        }
        if (degrees == 270) {
            return "west";
        }
        return "north";
    }

    public static String getPlayerStringLookDirection(Player player) {
        return Functions.getStringLookDirection(player.getViewYRot(1.0f));
    }

    public static String getStringLookDirection(float yaw) {
        if (yaw > 360.0f) {
            yaw %= 360.0f;
        }
        if (yaw < 0.0f) {
            yaw %= 360.0f;
            yaw += 360.0f;
        }
        if (yaw >= 315.0f || yaw < 45.0f) {
            return "south";
        }
        if (yaw < 135.0f) {
            return "west";
        }
        if (yaw < 225.0f) {
            return "north";
        }
        if (yaw < 315.0f) {
            return "east";
        }
        return "north";
    }

    public static String getBlockByID(Integer id) {
        return "minecraft:" + AllBlocksArray.blocksArray[id];
    }

    public static Integer getBlockIdByName(String name) {
        if (Arrays.asList(AllBlocksArray.blocksArray).contains(name)) {
            return ArrayUtils.indexOf((Object[])AllBlocksArray.blocksArray, (Object)name.replace("minecraft:", ""));
        }
        return null;
    }

    public static String getBlockByIdOrName(Object nameOrId) {
        try {
            int index = Math.round(Float.parseFloat(nameOrId.toString()));
            if (index >= AllBlocksArray.blocksArray.length || index < 0) {
                ++newerBlocksAmountFound;
                return "minecraft:air";
            }
            return "minecraft:" + AllBlocksArray.blocksArray[index];
        }
        catch (NumberFormatException e) {
            return nameOrId.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            return "minecraft:air";
        }
    }

    public static String getBlockNameAtPosition(Level world, Vector3d pos) {
        Block block = world.getBlockState(new BlockPos((int)pos.x, (int)pos.y, (int)pos.z)).getBlock();
        return BuiltInRegistries.BLOCK.getKey((Object)block).toString();
    }

    public static String getBlocknameFromBlock(Block block) {
        return BuiltInRegistries.BLOCK.getKey((Object)block).toString();
    }

    public static String PosToString(Vector3d pos) {
        return Math.round(pos.x) + " " + Math.round(pos.y) + " " + Math.round(pos.z);
    }

    public static String PosToString(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    public static Boolean hasPremiumMemberLevel(String memberLevel) {
        return memberLevel.equals("plus") || memberLevel.equals("pro");
    }

    public static void executeCommand(Level world, String command) {
        try {
            ServerLevel serverLevel = (ServerLevel)world;
            CommandSourceStack source = serverLevel.getServer().createCommandSourceStack().withLevel(serverLevel);
            serverLevel.getServer().getCommands().performPrefixedCommand(source, command);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

