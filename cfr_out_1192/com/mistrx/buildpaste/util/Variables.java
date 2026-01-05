/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.util;

import com.google.gson.JsonObject;
import com.mojang.math.Vector3d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.world.entity.player.Player;

public class Variables {
    public static List<Object> lastBlockIDs;
    public static List<String> lastBlockData;
    public static JsonObject lastNBT;
    public static Vector3d lastPos;
    public static Vector3d lastBlockSize;
    public static String lastPasteDirection;
    public static Vector3d lastFirstPos;
    public static Vector3d lastSecondPos;
    public static ArrayList<String> blocksInBuild;
    public static Player player;
    public static String mcname;
    public static String uuid;
    public static String buildID;
    public static String uploadedBuildID;
    public static HashMap<String, Integer> playerItems;
    public static HashMap<String, Integer> buildBlocks;
    public static String suggestion;
    public static String url;
    public static String userDataResponseString;
    public static String memberLevel;
    public static int incompatibleBlocksAmount;
    public static List<String> incompatibleBlocksExampleBlocksReplacedArray;

    static {
        blocksInBuild = new ArrayList();
        mcname = "";
        uuid = "";
        buildID = "";
        uploadedBuildID = "";
        playerItems = new HashMap();
        buildBlocks = new HashMap();
        suggestion = "dontplaceair";
        url = "https://buildpaste.net";
        userDataResponseString = "";
        memberLevel = "free";
        incompatibleBlocksAmount = 0;
        incompatibleBlocksExampleBlocksReplacedArray = new ArrayList<String>();
    }
}

