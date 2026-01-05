/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.util;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;

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
    public static String selectedBuildId;
    public static String uploadedBuildID;
    public static HashMap<String, Integer> playerItems;
    public static HashMap<String, Integer> buildBlocks;
    public static final String URL = "https://buildpaste.net";
    public static final String SUPPORT_EMAIL = "support@buildpaste.net";
    public static final int TOTAL_BUILDS_AMOUNT = 10000;
    public static String userDataResponseString;
    public static int incompatibleBlocksAmount;
    public static List<String> incompatibleBlocksExampleBlocksReplacedArray;

    static {
        lastPos = null;
        blocksInBuild = new ArrayList();
        mcname = "";
        uuid = "";
        selectedBuildId = "";
        uploadedBuildID = "";
        playerItems = new HashMap();
        buildBlocks = new HashMap();
        userDataResponseString = "";
        incompatibleBlocksAmount = 0;
        incompatibleBlocksExampleBlocksReplacedArray = new ArrayList<String>();
    }
}

